package org.jensens.service.account;

import org.jensens.service.account.storage.Account;
import org.jensens.service.account.storage.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AccountController {
    @Autowired
    private AccountService accountService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String AUTH_SUCCESS_MESSAGE = "{\"status\":\"success\", \"roles\":\"\"}";
    private static final String AUTH_FAIL_MESSAGE = "{\"status\":\"fail\"}";

    private static final String CREATE_SUCCESS_MESSAGE = "{\"status\":\"success\", \"accountId\":%d}";
    private static final String CREATE_FAIL_MESSAGE = "{\"status\":\"fail\"}";

    @RequestMapping(value = "v1/accounts/authenticate", method = RequestMethod.POST)
    public ResponseEntity<String> authenticate(@RequestParam(value="id") long accountId, @RequestParam(value="password") String password, HttpServletRequest request) {
        try {
            if (accountService.authenticatePassword(accountId, password)) {
                return ResponseEntity.ok(AUTH_SUCCESS_MESSAGE);
            }
            else {
                return ResponseEntity.ok(AUTH_FAIL_MESSAGE);
            }
        } catch (DataAccessException dax) {
            log.error("Request: " + request.getRequestURL() + " raised " + dax);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Data Access Error");
        } catch (PasswordException px) {
            log.error("Request: " + request.getRequestURL() + " raised " + px);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Password Processing Error");
        } catch (Exception ex) {
            log.error("Request: " + request.getRequestURL() + " raised " + ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping(value = "v1/accounts/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> getAccount(@PathVariable(value="id") long accountId, HttpServletRequest request) {
        try {
            return ResponseEntity.ok(accountService.getAccountJson(accountId));
        } catch (DataAccessException dax) {
            log.error("Request: " + request.getRequestURL() + " raised " + dax);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Data Access Error");
        } catch (Exception e) {
            log.error("Request: " + request.getRequestURL() + " raised " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping(value = "v1/accounts/list", method = RequestMethod.GET)
    public ResponseEntity<String> listAccounts(@RequestParam(value="limit", required=false) String limitStr, HttpServletRequest request) {
        long limit = Long.MAX_VALUE;

        if (limitStr != null) {
            try {
                limit = Long.parseLong(limitStr);
            } catch (NumberFormatException e) {
                log.error("Request: " + request.getRequestURL() + " raised " + e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }
        
        try {
            return ResponseEntity.ok(accountService.getAccountsAsJson(limit));
        } catch (DataAccessException dax) {
            log.error("Request: " + request.getRequestURL() + " raised " + dax);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Data Access Error");
        } catch (Exception e) {
            log.error("Request: " + request.getRequestURL() + " raised " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping(value = "v1/accounts/create", method = RequestMethod.POST)
    public ResponseEntity<String> create(@RequestParam(value="loginName") String loginName,
                          @RequestParam(value="firstName") String firstName,
                          @RequestParam(value="lastName") String lastName,
                          @RequestParam(value="password") String password,
                                                          HttpServletRequest request) {
        try {
            Account newAccount = accountService.createAccount(loginName, firstName, lastName, password);
            return ResponseEntity.ok(String.format(CREATE_SUCCESS_MESSAGE, newAccount.id));
        } catch (DataAccessException dax) {
            log.error("Request: " + request.getRequestURL() + " raised " + dax);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Data Access Error");
        } catch (PasswordException px) {
            log.error("Request: " + request.getRequestURL() + " raised " + px);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Password Processing Error");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(CREATE_FAIL_MESSAGE);
        }
    }
}
