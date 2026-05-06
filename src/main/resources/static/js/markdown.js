(function () {
    function fallbackEscape(text) {
        return String(text ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    function normalizeSource(markdown) {
        return String(markdown ?? "")
            .replace(/^[\u200B\u200C\u200D\u200E\u200F\uFEFF]/, "")
            .replace(/\r\n/g, "\n")
            .replace(/\r/g, "\n");
    }

    function createRenderer(allowRawHtml) {
        if (!window.markdownit) {
            return null;
        }

        var instance = window.markdownit({
            html: allowRawHtml,
            linkify: true,
            breaks: true,
            typographer: true
        });

        if (window.markdownitFootnote) {
            instance.use(window.markdownitFootnote);
        }

        if (window.markdownitTaskLists) {
            instance.use(window.markdownitTaskLists, {
                enabled: true,
                label: true,
                labelAfter: true
            });
        }

        return instance;
    }

    var rendererCache = {
        safe: null,
        rich: null
    };

    function getRenderer() {
        var allowRawHtml = window.DOMPurify && typeof window.DOMPurify.sanitize === "function";
        var cacheKey = allowRawHtml ? "rich" : "safe";

        if (!rendererCache[cacheKey]) {
            rendererCache[cacheKey] = createRenderer(allowRawHtml);
        }

        return rendererCache[cacheKey];
    }

    function sanitize(html) {
        if (window.DOMPurify && typeof window.DOMPurify.sanitize === "function") {
            return window.DOMPurify.sanitize(html, {
                ADD_TAGS: [
                    "details",
                    "summary",
                    "iframe",
                    "video",
                    "audio",
                    "source",
                    "figure",
                    "figcaption",
                    "input",
                    "label"
                ],
                ADD_ATTR: [
                    "class",
                    "open",
                    "controls",
                    "autoplay",
                    "muted",
                    "loop",
                    "playsinline",
                    "poster",
                    "preload",
                    "allowfullscreen",
                    "allow",
                    "frameborder",
                    "loading",
                    "referrerpolicy",
                    "width",
                    "height",
                    "colspan",
                    "rowspan",
                    "type",
                    "checked",
                    "disabled",
                    "for"
                ]
            });
        }
        return html;
    }

    function renderParagraphFallback(source) {
        return source
            .split(/\n{2,}/)
            .map(function (block) {
                return block.trim();
            })
            .filter(Boolean)
            .map(function (block) {
                return "<p>" + fallbackEscape(block).replace(/\n/g, "<br>") + "</p>";
            })
            .join("");
    }

    function renderToHtml(markdown) {
        var source = normalizeSource(markdown);
        if (!source) {
            return "";
        }

        var renderer = getRenderer();
        var html = renderer ? renderer.render(source) : renderParagraphFallback(source);
        return sanitize(html);
    }

    function renderMath(element) {
        if (!element || typeof window.renderMathInElement !== "function") {
            return;
        }

        window.renderMathInElement(element, {
            delimiters: [
                { left: "$$", right: "$$", display: true },
                { left: "\\[", right: "\\]", display: true },
                { left: "$", right: "$", display: false },
                { left: "\\(", right: "\\)", display: false }
            ],
            throwOnError: false,
            strict: "ignore",
            ignoredTags: ["script", "noscript", "style", "textarea", "pre", "code"]
        });
    }

    function enhanceTables(element) {
        if (!element) {
            return;
        }

        element.querySelectorAll("table").forEach(function (table) {
            table.classList.add("markdown-table");

            if (table.parentElement && table.parentElement.classList.contains("table-scroll")) {
                return;
            }

            var wrapper = document.createElement("div");
            wrapper.className = "table-scroll";
            table.parentNode.insertBefore(wrapper, table);
            wrapper.appendChild(table);
        });
    }

    function enhanceEmbeds(element) {
        if (!element) {
            return;
        }

        element.querySelectorAll("iframe, video").forEach(function (media) {
            media.classList.add("embedded-media");

            if (media.parentElement && media.parentElement.classList.contains("media-frame")) {
                return;
            }

            var wrapper = document.createElement("div");
            wrapper.className = "media-frame";
            media.parentNode.insertBefore(wrapper, media);
            wrapper.appendChild(media);
        });

        element.querySelectorAll("details").forEach(function (details) {
            details.classList.add("markdown-details");
        });

        element.querySelectorAll("summary").forEach(function (summary) {
            summary.classList.add("markdown-summary");
        });

        element.querySelectorAll("audio").forEach(function (audio) {
            audio.classList.add("embedded-audio");
        });
        normalizeTaskLists(element);
    }

    function findTaskMarkerTarget(item) {
        for (var i = 0; i < item.childNodes.length; i += 1) {
            var node = item.childNodes[i];

            if (node.nodeType === Node.TEXT_NODE) {
                if (!node.textContent || !node.textContent.trim()) {
                    continue;
                }
                return node;
            }

            if (node.nodeType === Node.ELEMENT_NODE) {
                if (node.tagName === "P" || node.tagName === "SPAN") {
                    for (var j = 0; j < node.childNodes.length; j += 1) {
                        var nestedNode = node.childNodes[j];
                        if (nestedNode.nodeType === Node.TEXT_NODE && nestedNode.textContent && nestedNode.textContent.trim()) {
                            return nestedNode;
                        }
                    }
                }

                if (node.textContent && node.textContent.trim()) {
                    return null;
                }
            }
        }

        return null;
    }

    function ensureTaskCheckbox(item, checked) {
        var checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.disabled = true;
        checkbox.tabIndex = -1;
        checkbox.setAttribute("aria-hidden", "true");
        checkbox.checked = checked;

        item.insertBefore(checkbox, item.firstChild);
        return checkbox;
    }

    function normalizeTaskLists(element) {
        if (!element) {
            return;
        }

        element.querySelectorAll("li").forEach(function (item) {
            var directCheckbox = item.querySelector(':scope > label > input[type="checkbox"], :scope > input[type="checkbox"]');

            if (!directCheckbox) {
                var targetNode = findTaskMarkerTarget(item);
                if (targetNode) {
                    var match = targetNode.textContent.match(/^\s*\[([ xX])\]\s+/);
                    if (match) {
                        targetNode.textContent = targetNode.textContent.replace(match[0], "");
                        directCheckbox = ensureTaskCheckbox(item, match[1].toLowerCase() === "x");
                    }
                }
            }

            if (directCheckbox) {
                item.classList.add("task-list-item");
                item.classList.toggle("is-checked", directCheckbox.checked || directCheckbox.hasAttribute("checked"));

                if (item.parentElement && (item.parentElement.tagName === "UL" || item.parentElement.tagName === "OL")) {
                    item.parentElement.classList.add("task-list");
                }
            }
        });
    }

    function renderInto(element, markdown) {
        if (!element) {
            return;
        }

        element.innerHTML = renderToHtml(markdown);
        enhanceTables(element);
        enhanceEmbeds(element);
        renderMath(element);
    }

    function stripMarkdown(markdown) {
        var source = normalizeSource(markdown)
            .replace(/```[\s\S]*?```/g, " ")
            .replace(/`[^`]*`/g, " ")
            .replace(/!\[[^\]]*\]\([^)]*\)/g, " ")
            .replace(/\[[^\]]*\]\([^)]*\)/g, " ")
            .replace(/^\[\^[^\]]+\]:.*$/gm, " ")
            .replace(/\[\^[^\]]+\]/g, " ")
            .replace(/\$\$[\s\S]*?\$\$/g, " ")
            .replace(/\\\[[\s\S]*?\\\]/g, " ")
            .replace(/\\\([\s\S]*?\\\)/g, " ")
            .replace(/\$[^$\n]+\$/g, " ")
            .replace(/<[^>]+>/g, " ")
            .replace(/[#>*_\-\n\r]/g, " ")
            .replace(/\s+/g, " ")
            .trim();

        if (source) {
            return source;
        }

        var container = document.createElement("div");
        container.innerHTML = renderToHtml(markdown);
        return (container.textContent || container.innerText || "")
            .replace(/\s+/g, " ")
            .trim();
    }

    window.KiblogMarkdown = {
        renderToHtml: renderToHtml,
        renderInto: renderInto,
        stripMarkdown: stripMarkdown
    };
})();
