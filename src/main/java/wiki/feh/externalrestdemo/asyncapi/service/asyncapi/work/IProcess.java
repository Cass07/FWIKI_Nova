package wiki.feh.externalrestdemo.asyncapi.service.asyncapi.work;

import wiki.feh.externalrestdemo.asyncapi.domain.AsyncResult;
import wiki.feh.externalrestdemo.asyncapi.dto.OpenAPIRequestBody;

public interface IProcess {
    public AsyncResult run(AsyncResult asyncResult, OpenAPIRequestBody openAPIRequestBody) throws Exception;
}
