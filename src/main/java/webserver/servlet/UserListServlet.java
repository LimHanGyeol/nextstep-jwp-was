package webserver.servlet;

import db.DataBase;
import model.User;
import utils.HandlebarsUtils;
import webserver.http.HttpHeader;
import webserver.http.HttpHeaders;
import webserver.http.request.HttpRequest;
import webserver.http.response.HttpResponse;
import webserver.http.response.ResponseBody;
import webserver.http.response.ResponseLine;
import webserver.http.session.SessionManager;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class UserListServlet extends HttpServlet {

    private static final String TEMPLATE_KEY = "users";
    private static final String REQUEST_PATH = "/user/list.html";
    private static final String REDIRECT_PATH = "/index.html";

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    protected HttpResponse doGet(HttpRequest request) {
        boolean hasSignIn = isSignIn(request.getHttpHeaders());
        if (hasSignIn) {
            Collection<User> users = DataBase.findAll();

            String page = applyHandlebars(users);
            byte[] pageBody = page.getBytes(StandardCharsets.UTF_8);

            ResponseLine responseLine = ResponseLine.ok();

            HttpHeaders httpHeaders = HttpHeaders.init();
            httpHeaders.addResponseHeader(HttpHeader.CONTENT_TYPE, "text/html;charset=utf-8");
            httpHeaders.addResponseHeader(HttpHeader.CONTENT_LENGTH, String.valueOf(pageBody.length));

            ResponseBody responseBody = ResponseBody.from(pageBody);

            return new HttpResponse(responseLine, httpHeaders, responseBody);
        }
        HttpHeaders httpHeaders = HttpHeaders.redirect(REDIRECT_PATH);
        return HttpResponse.redirect(httpHeaders);
    }

    public boolean isSignIn(HttpHeaders httpHeaders) {
        return httpHeaders.getCookies().hasCookie(SessionManager.SESSION_KEY);
    }

    private String applyHandlebars(Collection<User> users) {
        final String requestPath = "/user/list";
        HandlebarsUtils<User> userHandlebarsUtils = new HandlebarsUtils<>(requestPath, TEMPLATE_KEY, users);
        return userHandlebarsUtils.apply();
    }
}
