package by.zhuk.buber.controller;

import by.zhuk.buber.command.CommandType;
import by.zhuk.buber.command.ajax.AJAXCommandType;
import by.zhuk.buber.constant.CommandConstant;
import by.zhuk.buber.constant.PagesConstant;
import by.zhuk.buber.constant.UserConstant;
import by.zhuk.buber.exeption.ReceiverException;
import by.zhuk.buber.model.User;
import by.zhuk.buber.receiver.UserReceiver;
import by.zhuk.buber.validator.SignInValidator;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebFilter(urlPatterns = {"/*"}, filterName = "signIn")
public class SignInFilter implements Filter {
    private static Logger logger = LogManager.getLogger(SignInFilter.class);
    private static final String BANNED_ERROR = "bannedError";
    private static final String JS_FILE_EXPANSION = ".js";
    private static final String CSS_FILE_EXPANSION = ".css";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        HttpSession session = request.getSession();
        if (isLoginCommand(request.getParameter(CommandConstant.COMMAND)) || request.getRequestURI().startsWith(PagesConstant.SING_UP_PAGE) || requestFile(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SignInValidator.isAuthorization(session)) {
            UserReceiver receiver = new UserReceiver();
            String login = (String) session.getAttribute(UserConstant.LOGIN);
            Optional<User> userOptional = Optional.empty();
            try {
                userOptional = receiver.findUserByLogin(login);
            } catch (ReceiverException e) {
                logger.catching(e);
                response.sendRedirect(PagesConstant.ERROR_PAGE);
            }
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (!user.isBaned()) {
                    filterChain.doFilter(request, response);
                } else {
                    session.removeAttribute(UserConstant.LOGIN);
                    session.removeAttribute(UserConstant.TYPE);
                    request.setAttribute(BANNED_ERROR, true);
                    request.getRequestDispatcher(PagesConstant.LOGIN_PAGE).forward(request, response);
                }
            } else {
                logger.log(Level.WARN, "User is authorization [" + UserConstant.LOGIN + "],but not found in DB");
                session.removeAttribute(UserConstant.LOGIN);
                session.removeAttribute(UserConstant.TYPE);
                response.sendRedirect(PagesConstant.LOGIN_PAGE);
            }


        } else {
            request.getRequestDispatcher(PagesConstant.LOGIN_PAGE).forward(request, response);
        }
    }

    private boolean requestFile(String requestURI) {
        return requestURI.endsWith(CSS_FILE_EXPANSION) || requestURI.endsWith(JS_FILE_EXPANSION);
    }

    private boolean isLoginCommand(String command) {
        if (command == null) {
            return false;
        }
        command = command.toUpperCase().replaceAll("-", "_");

        boolean isLangCommand = command.equals(CommandType.LANG.name());
        boolean isSignInCommand = command.equals(AJAXCommandType.SIGN_IN.name());
        boolean isSignUpAcceptCommand = command.equals(CommandType.SIGN_UP_ACCEPT.name());
        boolean isSignUpUserCommand = command.equals(AJAXCommandType.SIGN_UP_USER.name());
        boolean isOAuthCommand = command.equals(CommandType.OAUTH.name()) || command.equals(CommandType.OAUTH_ACCEPT.name());

        return isSignInCommand || isSignUpAcceptCommand || isSignUpUserCommand || isOAuthCommand || isLangCommand;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init(FilterConfig filterConfig) {

    }

}