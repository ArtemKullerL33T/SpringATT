package com.example.springatt.controllers;

import com.example.springatt.repositories.ProductRepository;
import com.example.springatt.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/product")
public class ProductController {
    private  final ProductService productService;
    private  final ProductRepository productRepository;


    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @GetMapping("")
    public String getAllProduct(Model model){
        model.addAttribute("products", productService.getAllProduct());
        return "/product/product";
    }

    @GetMapping("/info/{id}")
    public String infoProduct(@PathVariable("id") int id, Model model){
        model.addAttribute("product", productService.getProductId(id));
        return "/product/infoProduct";
    }

    @PostMapping("/search")
    public String productSearch(@RequestParam("search") String search, @RequestParam("from1") String from1, @RequestParam("to1") String to1, @RequestParam(value = "price", required = false, defaultValue = "")String price, @RequestParam(value = "contract", required = false, defaultValue = "")String contract, Model model){

        if(!from1.isEmpty() & !to1.isEmpty()){
            if(!price.isEmpty()){
                if(price.equals("sorted_by_price_up")) {
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
                } else if(price.equals("sorted_by_price_down")){
                    if(!contract.isEmpty()){
                        System.out.println(contract);
                        if(contract.equals("electronic")){
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 1));
                        }else if (contract.equals("food")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 2));
                        } else if (contract.equals("clothes")) {
                            model.addAttribute("search_product", productRepository.findByTitleAndCategoryOrderByPriceDesc(search.toLowerCase(), Float.parseFloat(from1), Float.parseFloat(to1), 3));
                        }
                    }  else {
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
}
