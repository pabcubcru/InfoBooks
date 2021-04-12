package com.pabcubcru.infobooks.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pabcubcru.infobooks.models.Book;
import com.pabcubcru.infobooks.models.Request;
import com.pabcubcru.infobooks.models.RequestStatus;
import com.pabcubcru.infobooks.models.User;
import com.pabcubcru.infobooks.services.BookService;
import com.pabcubcru.infobooks.services.RequestService;
import com.pabcubcru.infobooks.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = "/requests")
public class RequestController {
    
    @Autowired
    private RequestService requestService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @GetMapping(value = {"/me", "/received"})
    public ModelAndView mainWithSecurity() {
        ModelAndView model = new ModelAndView();
        model.setViewName("Main");
        return model;
    }

    @GetMapping(value = {"/{id}/add"})
    public ModelAndView mainWithSecurity(@PathVariable("id") String id) {
        ModelAndView model = new ModelAndView();
        model.setViewName("Main");
        if(id != null) {
            Book book = this.bookService.findBookById(id);
            if(book == null) {
                model.setViewName("errors/Error404");
            }
        }
        return model;
    }

    @PostMapping(value = "/{id}/new")
    public Map<String, Object> addRequest(@RequestBody Request request, @PathVariable("id") String id, Principal principal) {
        Map<String, Object> res = new HashMap<>();

        try {
            Request req = this.requestService.findByUsername1AndIdBook2(principal.getName(), id);
            if(req == null) {
                Book book = this.bookService.findBookById(id);
                if(book.getAction().equals("INTERCAMBIO")) {
                    request.setAction("INTERCAMBIO");
                } else if(book.getAction().equals("VENTA")) {
                    request.setAction("VENTA");
                    request.setIdBook1("");
                }
                request.setIdBook2(id);
                request.setStatus(RequestStatus.PENDIENTE.toString());
                request.setUsername1(principal.getName());
                request.setUsername2(book.getUsername());
                this.requestService.save(request);
                res.put("success", true);
            } else {
                res.put("success", false);
            }
        } catch (Exception e) {
            res.put("success", false);
        }

        return res;
    }

    @GetMapping("/my-requests")
    public Map<String, Object> listMyRequest(Principal principal) {
        Map<String, Object> res = new HashMap<>();
        List<Book> books1 = new ArrayList<>();
        List<Book> books2 = new ArrayList<>();
        List<User> users = new ArrayList<>();

        List<Request> requests = this.requestService.listMyRequests(principal.getName());

        for(Request r : requests){
            if(r.getAction().equals("INTERCAMBIO")) {
                books1.add(this.bookService.findBookById(r.getIdBook1()));
            } else {
                books1.add(null);
            }
            if(r.getStatus().equals(RequestStatus.ACEPTADA.toString())) {
                users.add(this.userService.findByUsername(r.getUsername2()));
            } else{
                users.add(null);
            }
            books2.add(this.bookService.findBookById(r.getIdBook2()));
        }

        res.put("requests", requests);
        res.put("books1", books1);
        res.put("books2", books2);
        res.put("users", users);

        return res;
    }

    @GetMapping("/received-requests")
    public Map<String, Object> listReceivedRequest(Principal principal) {
        Map<String, Object> res = new HashMap<>();
        List<Book> books1 = new ArrayList<>();
        List<Book> books2 = new ArrayList<>();
        List<User> users = new ArrayList<>();

        List<Request> requests = this.requestService.listReceivedRequests(principal.getName());

        for(Request r : requests){
            if(r.getAction().equals("INTERCAMBIO")) {
                books1.add(this.bookService.findBookById(r.getIdBook1()));
            } else {
                books1.add(null);
            }
            books2.add(this.bookService.findBookById(r.getIdBook2()));
            if(r.getStatus().equals(RequestStatus.ACEPTADA.toString())) {
                users.add(this.userService.findByUsername(r.getUsername1()));
            } else{
                users.add(null);
            }
        }

        res.put("requests", requests);
        res.put("books1", books1);
        res.put("books2", books2);
        res.put("users", users);

        return res;
    }

    @GetMapping(value = "/{id}/cancel")
    public void cancelRequest(@PathVariable("id") String id) {
        Request request = this.requestService.findById(id);

        if(request.getStatus().equals(RequestStatus.PENDIENTE.toString())) {
            request.setStatus(RequestStatus.CANCELADA.toString());
            this.requestService.save(request);
        }
    }

    @DeleteMapping(value = "/{id}/delete")
    public void deleteRequest(@PathVariable("id") String id) {
        Request request = this.requestService.findById(id);

        if(request.getStatus().equals(RequestStatus.CANCELADA.toString()) || request.getStatus().equals(RequestStatus.RECHAZADA.toString())) {
            this.requestService.deleteById(id);
        }
    }

    @GetMapping("/{id}/accept")
    public ModelAndView acceptRequest(@PathVariable("id") String id, Principal principal) {
        Request request = this.requestService.findById(id);

        if(request.getUsername2().equals(principal.getName())) {
            if(request.getAction().equals("VENTA")) {
                List<Request> requests = this.requestService.findByIdBook2AndStatusNotAndStatusNotAndAction(request.getIdBook2(), "VENTA");
                requests.remove(request);
                for(Request r : requests) {
                    if(r.getStatus().equals(RequestStatus.PENDIENTE.toString())) {
                        r.setStatus(RequestStatus.RECHAZADA.toString());
                    }
                }
                this.requestService.saveAll(requests);
                request.setStatus(RequestStatus.ACEPTADA.toString());
                this.requestService.save(request);
            } else {
                List<Request> requests = this.requestService.findByIdBook1AndStatusNotAndStatusNotAndAction(request.getIdBook1(), "INTERCAMBIO");
                requests.addAll(this.requestService.findByIdBook2AndStatusNotAndStatusNotAndAction(request.getIdBook2(), "INTERCAMBIO"));
                requests.remove(request);
                for(Request r : requests) {
                    if(r.getStatus().equals(RequestStatus.PENDIENTE.toString())) {
                        r.setStatus(RequestStatus.RECHAZADA.toString());
                    }
                }
                this.requestService.saveAll(requests);
                request.setStatus(RequestStatus.ACEPTADA.toString());
                this.requestService.save(request);
            }
            return null;
        } else {
            ModelAndView model = new ModelAndView();
            model.setViewName("errors/Error403");
            return model;
        }
    }

    @GetMapping("/{id}/reject")
    public ModelAndView rejectRequest(@PathVariable("id") String id, Principal principal) {
        Request request = this.requestService.findById(id);

        if(request.getUsername2().equals(principal.getName())) {
            if(request.getStatus().equals(RequestStatus.PENDIENTE.toString())) {

                request.setStatus(RequestStatus.RECHAZADA.toString());
                this.requestService.save(request);
            }
            return null;
        } else {
            ModelAndView model = new ModelAndView();
            model.setViewName("errors/Error403");
            return model;
        }
    }
}