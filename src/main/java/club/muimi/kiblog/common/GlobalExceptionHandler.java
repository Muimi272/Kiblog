package club.muimi.kiblog.common;

import club.muimi.kiblog.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public Result<Void> handleNotFound(NoSuchElementException e) {
        return Result.fail(404, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        return Result.fail(404, "请求的资源不存在");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Result<Void> handleResponseStatus(ResponseStatusException e) {
        return Result.fail(e.getStatusCode().value(), e.getReason());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("未捕获的异常", e);
        return Result.fail(500, "服务器内部错误");
    }
}
