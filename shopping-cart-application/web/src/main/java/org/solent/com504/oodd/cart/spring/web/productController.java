/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.solent.com504.oodd.cart.spring.web;


import javax.servlet.http.HttpSession;
import org.solent.com504.oodd.cart.dao.impl.ShoppingItemCatalogRepository;
import org.solent.com504.oodd.cart.model.dto.ShoppingItem;
import org.solent.com504.oodd.cart.model.dto.User;
import org.solent.com504.oodd.cart.model.dto.UserRole;
import org.solent.com504.oodd.cart.model.service.ShoppingCart;
import org.solent.com504.oodd.cart.model.service.ShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

/**
 *
 * @author ISA06002471
 */

@Controller
@RequestMapping("/")
public class productController {
    
    @Autowired
    ShoppingService shoppingService = null;

    @Autowired
    ShoppingCart shoppingCart = null;
    
    @Autowired
    ShoppingItemCatalogRepository itemRepository;
    
    private User getSessionUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            sessionUser = new User();
            sessionUser.setUsername("anonymous");
            sessionUser.setUserRole(UserRole.ANONYMOUS);
            session.setAttribute("sessionUser", sessionUser);           
        }
        
        return sessionUser;
    }
    
    
    @RequestMapping(value = {"/editItem"}, method = RequestMethod.GET)
    
    public String editItem(
            @RequestParam(value = "itemName", required = false) String itemName,
            Model model,
            HttpSession session) {
        
        String message = "";
        String errorMessage = "";
        
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);
        
        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            errorMessage = "You do have permission to view";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            
            return "home";
        }
        
        if (itemName == null || itemName.isEmpty()) {
            model.addAttribute("errorMessage", "Cant search for an item with no name");
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("selectedPage", "admin");
            return "viewCatalog";
        }
        
        ShoppingItem editItem = itemRepository.findItemByName(itemName);
        
        if (editItem == null) {
            model.addAttribute("errorMessage", "No results found for " + itemName);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("selectedPage", "admin");
            return "viewCatalog";
        }
        
        model.addAttribute("editItem", editItem);
        return "editItem";
    }
    
    @RequestMapping(value = {"/editItem"}, method = RequestMethod.POST)
    
    public String updateItem(
            @RequestParam(value = "updateName", required = false) String updateItemName,
            @RequestParam(value = "currentName", required = false) String currentItemName,
            Model model,
            HttpSession session) {
        
        String message = "";
        String errorMessage = "";
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);
        
        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            errorMessage = "You do have permission to view";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            
            return "home";
        }
        
        if (updateItemName == null || updateItemName.isEmpty()) {
            model.addAttribute("errorMessage", "Cant update item with no name");
            model.addAttribute("editItem", itemRepository.findItemByName(currentItemName));
            model.addAttribute("selectedPage", "admin");
            return "editItem";
        }
        
        ShoppingItem editItem = itemRepository.findItemByName(currentItemName);
        
        if (editItem == null) {
            model.addAttribute("errorMessage", "No results found for " + currentItemName);
            model.addAttribute("editItem", itemRepository.findItemByName(currentItemName));
            model.addAttribute("selectedPage", "admin");
            return "editItem";
        }
        
        for(ShoppingItem i : shoppingCart.getShoppingCartItems()) {
            if(i.getName().equals(editItem.getName())) {
                i.setName(updateItemName);
            }
        }
        
        editItem.setName(updateItemName);
        editItem = itemRepository.save(editItem);
        model.addAttribute("editItem", editItem);
        return "editItem";
    }
    
}
