package org.solent.com504.oodd.cart.spring.web;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.solent.com504.oodd.cart.dao.impl.InvoiceRepository;
import org.solent.com504.oodd.cart.dao.impl.ShoppingItemCatalogRepository;
import org.solent.com504.oodd.cart.model.dto.Invoice;
import org.solent.com504.oodd.cart.model.dto.InvoiceState;
import org.solent.com504.oodd.cart.model.dto.ShoppingItem;
import org.solent.com504.oodd.cart.model.dto.User;
import org.solent.com504.oodd.cart.model.dto.UserRole;
import org.solent.com504.oodd.cart.model.service.ShoppingCart;
import org.solent.com504.oodd.cart.model.service.ShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/")
public class MVCController {

    final static Logger LOG = LogManager.getLogger(MVCController.class);
    final static Logger transactionLOG = LogManager.getLogger("Transaction_Logger");

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    private ShoppingItemCatalogRepository shoppingItemCatalogRepository;
    
    @Autowired
    ShoppingService shoppingService = null;

    @Autowired
    @Qualifier("shoppingServiceImpl")
    ShoppingCart shoppingCart = null;

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

    // Redirects calls to the root of our application to index.html
    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        return "redirect:/index.html";
    }

    @RequestMapping(value = "/home", method = {RequestMethod.GET, RequestMethod.POST})
    public String viewCart(@RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "itemName", required = false) String itemName,
            @RequestParam(name = "itemUUID", required = false) String itemUuid,
            Model model,
            HttpSession session) {

        // get sessionUser from session
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        // used to set tab selected
        model.addAttribute("selectedPage", "home");

        String message = "";
        String errorMessage = "";

        if (action == null) {
            // do nothing but show page
        } else if ("addItemToCart".equals(action)) {
            ShoppingItem shoppingItem = shoppingService.getNewItemByName(itemName);
            if (shoppingItem == null) {
                message = "cannot add unknown " + itemName + " to cart";
            } else {
                message = "adding " + itemName + " to cart price= " + shoppingItem.getPrice();
                shoppingCart.addItemToCart(shoppingItem);
            }
        } else if ("removeItemFromCart".equals(action)) {
            message = "removed " + itemName + " from cart";
            shoppingCart.removeItemFromCart(itemUuid);
        } else if ("purchaseItems".equals(action)) {
            if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole()) && !UserRole.CUSTOMER.equals(sessionUser.getUserRole())) {
                errorMessage = "You must be signed in to purchase.";
                model.addAttribute("errorMessage", errorMessage);
                model.addAttribute("availableItems", shoppingService.getAvailableItems());
                model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
                model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
                return "login";
            }
            List<ShoppingItem> shoppingCartItems = shoppingCart.getShoppingCartItems();
            if (shoppingCartItems.isEmpty()) {
                model.addAttribute("errorMessage", "Cart is empty, please add an item if you wish to buy");
                model.addAttribute("availableItems", shoppingService.getAvailableItems());
                model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
                model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
                return "home";
            }

            Invoice invoice = new Invoice();
            invoice.setUuid(UUID.randomUUID().toString());
            invoice.setDateOfPurchase(new Date());
            invoice.setAmountDue(shoppingCart.getTotal());

            List<ShoppingItem> itemList = new ArrayList<>();
            shoppingCartItems.forEach((cartItem) -> {
                ShoppingItem shoppingItem = new ShoppingItem(cartItem.getName(), cartItem.getPrice());
                shoppingItem.setQuantity(cartItem.getQuantity());
                shoppingItem.setUuid(UUID.randomUUID().toString());
                shoppingItemCatalogRepository.save(shoppingItem);
                itemList.add(shoppingItem);
            });

            invoice.setPurchasedItems(itemList);
            invoice.setPurchaser(sessionUser);
            invoice.setState(InvoiceState.PENDING);
            invoiceRepository.save(invoice);
            LOG.info("New invoice " + invoice.getUuid());
            transactionLOG.info("Purchase made by User " + sessionUser.getUsername() + "for amount £" + invoice.getAmountDue());

            shoppingCartItems.forEach((cartItem) -> {
                shoppingCart.removeItemFromCart(cartItem.getUuid());
            });

        } else {
            message = "unknown action=" + action;
        }

//        List<ShoppingItem> availableItems = shoppingService.getAvailableItems();
//
//        List<ShoppingItem> shoppingCartItems = shoppingCart.getShoppingCartItems();

        // populate model with values
        model.addAttribute("availableItems", shoppingService.getAvailableItems());
        model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
        model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);

        return "home";
    }

    @RequestMapping(value = "/about", method = {RequestMethod.GET, RequestMethod.POST})
    public String aboutCart(Model model, HttpSession session) {

        // get sessionUser from session
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        // used to set tab selected
        model.addAttribute("selectedPage", "about");
        return "about";
    }

    @RequestMapping(value = "/contact", method = {RequestMethod.GET, RequestMethod.POST})
    public String contactCart(Model model, HttpSession session) {

        // get sessionUser from session
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        // used to set tab selected
        model.addAttribute("selectedPage", "contact");
        return "contact";
    }

    @RequestMapping(value = "/viewCatalog", method = {RequestMethod.GET, RequestMethod.POST})
    public String itemCatalog(Model model, HttpSession session) {
        // get sessionUser from session
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);
        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            String errorMessage = "You do have permission to view";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            LOG.warn(errorMessage);
            return "home";
        }

        model.addAttribute("availableItems", shoppingService.getAvailableItems());
        model.addAttribute("selectedPage", "admin");
        LOG.info("Catalog opened successfully");
        return "viewCatalog";
    }

    @RequestMapping(value = "/viewModifyInvoice", method = RequestMethod.GET)
    public String invoice(
            @RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
            Model model,
            HttpSession session) {
        String message = "";
        String errorMessage = "";

        model.addAttribute("selectedPage", "home");

        LOG.info("GET viewModifyInvoice called.");

        // Ensure user has appropriate permissions to view, or is logged in 
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole()) && !UserRole.CUSTOMER.equals(sessionUser.getUserRole())) {
            errorMessage = "You must be signed in to view orders.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            return "home";
        }

        Invoice invoice = invoiceRepository.findByuuid(invoiceNumber);
        if (invoice == null) {
            LOG.warn("Invoice not found" + invoiceNumber);
            errorMessage = "Unknown invoice: " + invoiceNumber;
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            return "home";
        }
        List<String> iState = new ArrayList<>();
        for (InvoiceState s : InvoiceState.values()) {
            iState.add(s.toString());
        }
        model.addAttribute("state", iState);
        model.addAttribute("invoice", invoice);
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("message", message);
        model.addAttribute("errorMessage", errorMessage);
        return "viewModifyInvoice";
    }

    @RequestMapping(value = "/updateInvoiceState", method = {RequestMethod.GET, RequestMethod.POST})
    public String updateIState(
            @RequestParam(value = "invoiceNumber", required = false) String invoiceNumber,
            @RequestParam(value = "state", required = false) String state,
            Model model,
            HttpSession session
    ) {
        String message = "";
        String errorMessage = "";

        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            errorMessage = "ACCESS DENIED - Administrator privileges required.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            LOG.warn(errorMessage);
            return "home";
        }

        Invoice updateInvoice = invoiceRepository.findByuuid(invoiceNumber);
        if (updateInvoice == null) {
            LOG.warn("Invoice not found" + invoiceNumber);
            errorMessage = "Unknown invoice: " + invoiceNumber;
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            return "home";
        } else if (state == null) {
            LOG.warn("Cannot update invoice (" + invoiceNumber + ") state.");
            LOG.warn("Invoice not found" + invoiceNumber);
            errorMessage = "Unknown invoice: " + invoiceNumber;
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            return "home";
        } else {
            switch (state) {
                case "PENDING":
                    updateInvoice.setState(InvoiceState.PENDING);
                    break;
                case "FULFILLED":
                    updateInvoice.setState(InvoiceState.FULFILLED);
                    break;
                case "REJECTED":
                    updateInvoice.setState(InvoiceState.REJECTED);
                    break;
                default:
                    LOG.warn("Unable to update invoice " + invoiceNumber + " state to " + state);
                    errorMessage = "Unknown invoice: " + invoiceNumber;
                    model.addAttribute("errorMessage", errorMessage);
                    model.addAttribute("availableItems", shoppingService.getAvailableItems());
                    model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
                    model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
                    return "home";
            }

            invoiceRepository.save(updateInvoice);
            LOG.info("Invoice " + updateInvoice.getUuid() + " status updated to " + state);
            List<String> updateState = new ArrayList<>();
            for (InvoiceState s : InvoiceState.values()) {
                updateState.add(s.toString());
            }
            model.addAttribute("statusValues", state);
            model.addAttribute("invoice", updateInvoice);
            model.addAttribute("sessionUser", sessionUser);
            message = "Invoice updated";
            model.addAttribute("message", message);
            model.addAttribute("selectedPage", "admin");
            return "viewModifyInvoice";
        }
    }

    @RequestMapping(value = "/invoices", method = {RequestMethod.GET, RequestMethod.POST})
    public String listInvoices(
            Model model,
            HttpSession session) {

        String message = "";
        String errorMessage = "";

        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (!UserRole.ADMINISTRATOR.equals(sessionUser.getUserRole())) {
            errorMessage = "ACCESS DENIED - Administrator privileges required.";
            model.addAttribute("errorMessage", errorMessage);
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            LOG.warn(errorMessage);
            return "home";
        }

        List<Invoice> invoices = invoiceRepository.findAll();
        model.addAttribute("invoices", invoices);

        model.addAttribute("selectedPage", "admin");
        return "viewInvoiceAdmin";

    }

    @RequestMapping(value = "/viewInvoiceUser", method = {RequestMethod.GET, RequestMethod.POST})
    public String userInvoiceList(Model model, HttpSession session) {
        User sessionUser = getSessionUser(session);
        model.addAttribute("sessionUser", sessionUser);

        if (sessionUser.getUserRole() == UserRole.ANONYMOUS) {
            model.addAttribute("errorMessage", "Please sign in to view orders");
            model.addAttribute("availableItems", shoppingService.getAvailableItems());
            model.addAttribute("shoppingCartItems", shoppingCart.getShoppingCartItems());
            model.addAttribute("shoppingcartTotal", shoppingCart.getTotal());
            return "home";
        }

        List<Invoice> invoices = invoiceRepository.findByPurchaser(sessionUser);
        model.addAttribute("invoices", invoices);
        model.addAttribute("selectedPage", "viewInvoiceUser");
        return "viewInvoiceUser";
    }

    /*
     * Default exception handler, catches all exceptions, redirects to friendly
     * error page. Does not catch request mapping errors
     */
    @ExceptionHandler(Exception.class)
    public String myExceptionHandler(final Exception e, Model model,
            HttpServletRequest request
    ) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        final String strStackTrace = sw.toString(); // stack trace as a string
        String urlStr = "not defined";
        if (request != null) {
            StringBuffer url = request.getRequestURL();
            urlStr = url.toString();
        }
        model.addAttribute("requestUrl", urlStr);
        model.addAttribute("strStackTrace", strStackTrace);
        model.addAttribute("exception", e);
        LOG.error(strStackTrace);
        return "error";
    }

}
