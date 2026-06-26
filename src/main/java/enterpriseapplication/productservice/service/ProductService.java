package enterpriseapplication.productservice.service;


import enterpriseapplication.productservice.dto.ProductDTO;

import java.util.List;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    List<ProductDTO> getAllProducts();
    void deleteProductById(Long id);
}
