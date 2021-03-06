package com.example.carportal.controllers;

import com.example.carportal.models.*;
import com.example.carportal.repositories.CarRepository;
import com.example.carportal.repositories.LeaseRepository;
import com.example.carportal.repositories.UserRepository;
import com.example.carportal.services.DamageService;
import com.example.carportal.services.JoinService;
import com.example.carportal.services.LeaseService;
import com.example.carportal.services.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;

@Controller
public class LeaseController {

    private LeaseService ls = new LeaseService(new LeaseRepository());
    private JoinService js = new JoinService(new UserRepository(),new CarRepository());
    private DamageService ds = new DamageService();
    private SessionService ss = new SessionService();

    @GetMapping("/createlease")
    public String createLease(Model model, HttpSession session){
        ArrayList<Customer> allCustomers = js.getListOfCustomers();
        ArrayList<Car> availableCars = js.getCars(1);
        model.addAttribute("allCustomers", allCustomers);
        model.addAttribute("availableCars", availableCars);
        boolean hasAccess = ss.hasRegistrationRole(session);
        return (hasAccess) ? "createlease" : "redirect:/accessdenied";
    }

    @PostMapping("/createlease")
    public String createLease(WebRequest request){
        int carID = Integer.valueOf(request.getParameter("carID"));
        int customerID = Integer.valueOf(request.getParameter("customerID"));
        double price = Double.parseDouble(request.getParameter("price"));
        Date startDate = Date.valueOf(request.getParameter("startDate"));
        Date endDate = Date.valueOf(request.getParameter("endDate"));
        Lease lease = new Lease(carID, customerID, price, startDate.toLocalDate(), endDate.toLocalDate(), true);
        ls.createLease(lease);
        js.changeCarStatus(carID);
        return "redirect:/createleasesuccess";
    }

    @GetMapping("/createleasesuccess")
    public String leaseCreated(){
        return "createleasesuccess";
    }

    @GetMapping("/createdamagereport")
    public String getdata(Model model, HttpSession session)
    {
        ArrayList<Integer> openLeaseIds = new ArrayList<>(Arrays.asList(22, 37, 39));
        model.addAttribute("listOfDamages", ds.getSessionListOFDamages(session));
        model.addAttribute("openLeases", openLeaseIds);
        boolean hasAccess = ss.hasDamageRole(session);
        return (hasAccess) ? "createdamagereport" : "redirect:/accessdenied";
    }

    @PostMapping("/createdamagereport")
    public String gettingdata(WebRequest request, HttpSession session)
    {
        int leaseId = Integer.parseInt(request.getParameter("leaseId"));
        String desc = request.getParameter("description");
        Double price = Double.parseDouble(request.getParameter("price"));
        ds.getSessionListOFDamages(session).add(new Damage(leaseId, desc, price));
        return "redirect:/createdamagereport";
    }

    @GetMapping("createdamagereportsuccess")
    public String gotdata(Model model, HttpSession session)
    {
        model.addAttribute("listOfDamages", ds.getSessionListOFDamages(session));
        model.addAttribute("totalPrice", ds.getTotalDamage(session));
        System.out.println(ds.getTotalDamage(session));
        return "createdamagereportsuccess";
    }

    @GetMapping("viewmonthlyincome")
    public String viewmonthlyincome(HttpSession session, Model model)
    {
        ArrayList<Lease> leases = ls.getAllOpenLeases();
        ArrayList<Statistic> stats = js.getListOfStatistics(leases);
        model.addAttribute("statistics", stats);
        model.addAttribute("numberOfLeasedCars" , leases.size());
        model.addAttribute("totalPrice", ls.calculateMonthlyEarnings());
        boolean hasAccess = ss.hasBusinessRole(session);
        return (hasAccess) ? "viewmonthlyincome" : "redirect:/accessdenied";
    }
}
