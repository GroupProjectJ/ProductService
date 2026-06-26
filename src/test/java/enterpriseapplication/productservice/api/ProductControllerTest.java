package enterpriseapplication.productservice.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import enterpriseapplication.productservice.dto.ProductDTO;
import enterpriseapplication.productservice.exception.ResourceNotFoundException;
import enterpriseapplication.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleDTO = new ProductDTO();
        sampleDTO.setProductId(1L);
        sampleDTO.setName("Test Widget");
        sampleDTO.setUnitPrice(new BigDecimal("19.99"));
        sampleDTO.setDescription("A test widget");
        sampleDTO.setCategory("Electronics");
        sampleDTO.setStock(100);
    }

    // ---- POST /api/products ----

    @Test
    @DisplayName("POST /api/products: valid body returns 201 with response body")
    void createProduct_validBody_returns201() throws Exception {
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(sampleDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.name").value("Test Widget"))
                .andExpect(jsonPath("$.unitPrice").value(19.99))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(100));
        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    @DisplayName("POST /api/products: blank name returns 400 with field error")
    void createProduct_blankName_returns400WithErrors() throws Exception {
        ProductDTO invalid = new ProductDTO();
        invalid.setName("");
        invalid.setUnitPrice(new BigDecimal("5.00"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").value("Product name is required"));
        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products: null unitPrice returns 400 with field error")
    void createProduct_nullUnitPrice_returns400WithErrors() throws Exception {
        ProductDTO invalid = new ProductDTO();
        invalid.setName("Valid Name");
        invalid.setUnitPrice(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.unitPrice").value("Unit price is required"));
        verify(productService, never()).createProduct(any());
    }

    @Test
    @DisplayName("POST /api/products: negative unitPrice returns 400 with field error")
    void createProduct_negativeUnitPrice_returns400WithErrors() throws Exception {
        ProductDTO invalid = new ProductDTO();
        invalid.setName("Valid Name");
        invalid.setUnitPrice(new BigDecimal("-1.00"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.unitPrice").value("Unit price must be positive"));
        verify(productService, never()).createProduct(any());
    }

    // ---- GET /api/products/{id} ----

    @Test
    @DisplayName("GET /api/products/{id}: existing id returns 200 with product body")
    void getProductById_existingId_returns200() throws Exception {
        when(productService.getProductById(1L)).thenReturn(sampleDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.name").value("Test Widget"));
        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/products/{id}: non-existent id returns 404 with error body")
    void getProductById_nonExistentId_returns404() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
        verify(productService, times(1)).getProductById(99L);
    }

    // ---- GET /api/products ----

    @Test
    @DisplayName("GET /api/products: returns 200 with list of all products")
    void getAllProducts_returns200WithList() throws Exception {
        ProductDTO second = new ProductDTO();
        second.setProductId(2L);
        second.setName("Second Product");
        second.setUnitPrice(new BigDecimal("9.99"));
        List<ProductDTO> products = Arrays.asList(sampleDTO, second);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productId").value(1L))
                .andExpect(jsonPath("$[1].productId").value(2L))
                .andExpect(jsonPath("$[1].name").value("Second Product"));
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/products: empty list returns 200 with empty array")
    void getAllProducts_emptyList_returns200WithEmptyArray() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---- DELETE /api/products/{id} ----

    @Test
    @DisplayName("DELETE /api/products/{id}: existing id returns 204 No Content")
    void deleteProductById_existingId_returns204() throws Exception {
        doNothing().when(productService).deleteProductById(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
        verify(productService, times(1)).deleteProductById(1L);
    }

    @Test
    @DisplayName("DELETE /api/products/{id}: non-existent id returns 404 with error body")
    void deleteProductById_nonExistentId_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found with id: 99"))
                .when(productService).deleteProductById(99L);

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
        verify(productService, times(1)).deleteProductById(99L);
    }
}
