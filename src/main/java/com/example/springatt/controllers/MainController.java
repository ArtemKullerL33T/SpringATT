package com.example.springatt.controllers;

import com.example.springatt.enumm.Status;
import com.example.springatt.models.Cart;
import com.example.springatt.models.Order;
import com.example.springatt.models.Person;
import com.example.springatt.models.Product;
import com.example.springatt.repositories.CartRepository;
import com.example.springatt.repositories.OrderRepository;
import com.example.springatt.repositories.ProductRepository;
import com.example.springatt.security.PersonDetails;
import com.example.springatt.services.PersonService;
import com.example.springatt.services.ProductService;
import com.example.springatt.util.PersonValidator;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class MainController {
    private final PersonValidator personValidator;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final PersonService personService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public MainController(PersonValidator personValidator, CartRepository cartRepository, ProductService productService, PersonService personService, ProductRepository productRepository, OrderRepository orderRepository) {
        this.personValidator = personValidator;
        this.cartRepository = cartRepository;
        this.productService = productService;
        this.personService = personService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }
    @GetMapping("/lk")
    public String index(Model model){
        // получаем объект Authentication с помощью спрингКонтекстХолдер обращаемся к контексту и на нем вызываем метод Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        String role = personDetails.getPerson().getRole();
        if(role.equals("ROLE_ADMIN")){
            return "redirect:/admin";
        }

        System.out.println(personDetails.getPerson());
        System.out.println("ID пользователя " + personDetails.getPerson().getId());
        System.out.println("Login " + personDetails.getPerson().getLogin());
        System.out.println("Password " + personDetails.getPerson().getPassword());
        model.addAttribute("products", productService.getAllProduct());

        return "/personal/index";
    }
    @GetMapping("/registration")
    public String registration(@ModelAttribute("person") Person person){
        return "registration";
    }
    @PostMapping("/registration")
    public String resultRegistration(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult){
        personValidator.validate(person, bindingResult);
        if(bindingResult.hasErrors()){
            return "registration";
        }
        personService.register(person);
        return "redirect:/lk";
    }


    @GetMapping("/lk/product/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model){
        model.addAttribute("product", productService.getProductId(id));
        return "/personal/infoProduct";
    }


    @PostMapping("/lk/product/search")
    public String productSearch(@RequestParam("search") String search, @RequestParam("from1") String from1, @RequestParam("to1") String to1, @RequestParam(value = "price", required = false, defaultValue = "")String price, @RequestParam(value = "contract", required = false, defaultValue = "")String contract, Model model) {

        if (!from1.isEmpty() & !to1.isEmpty()) {
            if (!price.isEmpty()) {
                if (price.equals("sorted_by_price_up")) {
                    if (!contract.isEmpty()) {
                        if (contract.equals("electronic")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 1));
                        } else if (contract.equals("food")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 2));
                        } else if (contract.equals("clothes")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 3));
                        }
                    } else {
                        model.addAttribute("search_product", productRepository.findByTitleOrderByPriceAsc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1)));
                    }
                } else if (price.equals("sorted_by_price_down")) {
                    if (!contract.isEmpty()) {
                        System.out.println(contract);
                        if (contract.equals("electronic")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 1));
                        } else if (contract.equals("food")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 2));
                        } else if (contract.equals("clothes")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 3));
                        }
                    } else {
                        model.addAttribute("search_product", productRepository.findByTitleOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1)));
                    }
                }
            } else {
                model.addAttribute("search_product", productRepository.findByTitleAndPriceGreaterThanEqualAndPriceLessThanEqual(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1)));
            }
        } else {
            model.addAttribute("search_product", productRepository.findByTitleContainingIgnoreCase(search));
            }
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("value_search", search);
        model.addAttribute("value_price_from1", from1);
        model.addAttribute("value_price_to1", to1);
        return "/product/product";
    }
    @GetMapping("/cart/add/{id}")
    public String addProductInCart(@PathVariable("id") int id, Model model){
        Product product = productService.getProductId(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int id_person = personDetails.getPerson().getId();
        Cart cart = new Cart(id_person, product.getId());
        cartRepository.save(cart);
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        int id_person = personDetails.getPerson().getId();

        List<Cart> cartList = cartRepository.findByPersonId(id_person);
        List<Product> productList = new ArrayList<>();
        for (Cart cart: cartList) {
            productList.add(productService.getProductId(cart.getProductId()));
        }

        // Вычисление  цены
        float price = 0;
        for (Product product: productList) {
            price += product.getPrice();
        }

        model.addAttribute("price", price);
        model.addAttribute("cart_product", productList);
        return "/personal/cart";
    }
// удаление товара из корзины
        @GetMapping("/cart/delete/{id}")
        public String deleteProductFromCart(Model model, @PathVariable("id") int id){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();

            int id_person = personDetails.getPerson().getId();
            List<Cart> cartList = cartRepository.findByPersonId(id_person);
            List<Product> productList = new ArrayList<>();


            for (Cart cart: cartList) {
                productList.add(productService.getProductId(cart.getProductId()));
            }
            cartRepository.deleteCartByProductId(id);
            return "redirect:/cart";

    }
         @GetMapping("/order/create")
         public String order(){
             Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
             PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
             int id_person = personDetails.getPerson().getId();

             List<Cart> cartList = cartRepository.findByPersonId(id_person);
             List<Product> productList = new ArrayList<>();
             for (Cart cart: cartList) {
                 productList.add(productService.getProductId(cart.getProductId()));
             }

             // Вычисление  цены
             float price = 0;
             for (Product product: productList) {
                 price += product.getPrice();
             }

             String uuid = UUID.randomUUID().toString();
             for(Product product : productList){
                 Order newOrder = new Order(uuid, product, personDetails.getPerson(), 1, product.getPrice(), Status.Получен);
                 orderRepository.save(newOrder);
                 cartRepository.deleteCartByProductId(product.getId());
             }
             return "redirect:/orders";
    }
    @GetMapping("/orders")
    public String orderUser(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        List<Order> orderList = orderRepository.findByPerson(personDetails.getPerson());
        model.addAttribute("orders", orderList);
        return "/personal/orders";
    }
}

