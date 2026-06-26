package enterpriseapplication.productservice.service;

import enterpriseapplication.productservice.dto.ProductDTO;
import enterpriseapplication.productservice.exception.ResourceNotFoundException;
import enterpriseapplication.productservice.model.Product;
import enterpriseapplication.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private ProductDTO sampleProductDTO;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product(
                "Test Widget",
                new BigDecimal("19.99"),
                "A test widget",
                "Electronics",
                100
        );
        sampleProduct.setProductId(1L);

        sampleProductDTO = new ProductDTO();
        sampleProductDTO.setProductId(1L);
        sampleProductDTO.setName("Test Widget");
        sampleProductDTO.setUnitPrice(new BigDecimal("19.99"));
        sampleProductDTO.setDescription("A test widget");
        sampleProductDTO.setCategory("Electronics");
        sampleProductDTO.setStock(100);
    }

    // ----- createProduct -----

    @Test
    @DisplayName("createProduct: saved entity is mapped back to DTO with all fields")
    void createProduct_validDTO_returnsSavedDTO() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDTO result = productService.createProduct(sampleProductDTO);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Widget");
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(result.getDescription()).isEqualTo("A test widget");
        assertThat(result.getCategory()).isEqualTo("Electronics");
        assertThat(result.getStock()).isEqualTo(100);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct: all DTO fields flow through to the persisted entity")
    void createProduct_capturesCorrectFieldsOnSave() {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setProductId(99L);
            return p;
        });

        ProductDTO input = new ProductDTO();
        input.setName("New Product");
        input.setUnitPrice(new BigDecimal("5.00"));
        input.setDescription("Desc");
        input.setCategory("Category");
        input.setStock(50);

        ProductDTO result = productService.createProduct(input);

        assertThat(result.getName()).isEqualTo("New Product");
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(result.getDescription()).isEqualTo("Desc");
        assertThat(result.getCategory()).isEqualTo("Category");
        assertThat(result.getStock()).isEqualTo(50);
    }

    // ----- getProductById -----

    @Test
    @DisplayName("getProductById: existing id returns mapped DTO")
    void getProductById_existingId_returnsDTO() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Widget");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getProductById: non-existent id throws ResourceNotFoundException with correct message")
    void getProductById_nonExistentId_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 99");
        verify(productRepository, times(1)).findById(99L);
    }

    // ----- getAllProducts -----

    @Test
    @DisplayName("getAllProducts: returns a DTO for each entity returned by repository")
    void getAllProducts_multipleProducts_returnsMappedList() {
        Product second = new Product("Second", new BigDecimal("9.99"), "Desc2", "Cat2", 5);
        second.setProductId(2L);
        when(productRepository.findAll()).thenReturn(Arrays.asList(sampleProduct, second));

        List<ProductDTO> results = productService.getAllProducts();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getProductId()).isEqualTo(1L);
        assertThat(results.get(1).getProductId()).isEqualTo(2L);
        assertThat(results.get(1).getName()).isEqualTo("Second");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllProducts: empty repository returns empty list")
    void getAllProducts_emptyRepository_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        List<ProductDTO> results = productService.getAllProducts();

        assertThat(results).isEmpty();
        verify(productRepository, times(1)).findAll();
    }

    // ----- deleteProductById -----

    @Test
    @DisplayName("deleteProductById: existing id calls repository delete exactly once")
    void deleteProductById_existingId_deletesProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        productService.deleteProductById(1L);

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).delete(sampleProduct);
    }

    @Test
    @DisplayName("deleteProductById: non-existent id throws ResourceNotFoundException; delete never called")
    void deleteProductById_nonExistentId_throwsResourceNotFoundException() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProductById(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with id: 42");
        verify(productRepository, times(1)).findById(42L);
        verify(productRepository, never()).delete(any(Product.class));
    }
}
