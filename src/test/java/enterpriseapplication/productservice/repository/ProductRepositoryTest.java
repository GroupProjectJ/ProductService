package enterpriseapplication.productservice.repository;

import enterpriseapplication.productservice.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product persistedProduct;

    @BeforeEach
    void setUp() {
        Product product = new Product(
                "Test Widget",
                new BigDecimal("19.99"),
                "A test widget",
                "Electronics",
                100
        );
        persistedProduct = entityManager.persistAndFlush(product);
    }

    // ----- save -----

    @Test
    @DisplayName("save: new product is persisted with auto-generated id")
    void save_newProduct_persistsWithGeneratedId() {
        Product newProduct = new Product("New Item", new BigDecimal("29.99"), "Description", "Clothing", 25);

        Product saved = productRepository.save(newProduct);

        assertThat(saved.getProductId()).isNotNull().isPositive();
        assertThat(saved.getName()).isEqualTo("New Item");
        assertThat(saved.getUnitPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(saved.getCategory()).isEqualTo("Clothing");
        assertThat(saved.getStock()).isEqualTo(25);
    }

    @Test
    @DisplayName("save: product with null optional fields persists without error")
    void save_productWithNullOptionalFields_persists() {
        Product minimal = new Product("Minimal", new BigDecimal("1.00"), null, null, null);

        Product saved = productRepository.save(minimal);

        assertThat(saved.getProductId()).isNotNull();
        assertThat(saved.getDescription()).isNull();
        assertThat(saved.getCategory()).isNull();
        assertThat(saved.getStock()).isNull();
    }

    @Test
    @DisplayName("save on existing entity: updates the persisted record")
    void save_existingProduct_updatesRecord() {
        persistedProduct.setName("Updated Name");
        persistedProduct.setUnitPrice(new BigDecimal("99.99"));
        productRepository.save(persistedProduct);
        entityManager.flush();
        entityManager.clear();

        Product updated = entityManager.find(Product.class, persistedProduct.getProductId());

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getUnitPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    // ----- findById -----

    @Test
    @DisplayName("findById: existing id returns present Optional with correct fields")
    void findById_existingId_returnsProduct() {
        Optional<Product> found = productRepository.findById(persistedProduct.getProductId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Widget");
        assertThat(found.get().getUnitPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(found.get().getCategory()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("findById: non-existent id returns empty Optional")
    void findById_nonExistentId_returnsEmpty() {
        Optional<Product> found = productRepository.findById(Long.MAX_VALUE);

        assertThat(found).isEmpty();
    }

    // ----- findAll -----

    @Test
    @DisplayName("findAll: single persisted product returns list of size 1")
    void findAll_onePersisted_returnsListOfOne() {
        List<Product> all = productRepository.findAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getName()).isEqualTo("Test Widget");
    }

    @Test
    @DisplayName("findAll: two persisted products returns list of size 2")
    void findAll_multiplePersisted_returnsAll() {
        entityManager.persistAndFlush(new Product("Second", new BigDecimal("5.00"), null, "Books", 10));

        List<Product> all = productRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("findAll: empty table returns empty list")
    void findAll_emptyTable_returnsEmptyList() {
        entityManager.remove(entityManager.find(Product.class, persistedProduct.getProductId()));
        entityManager.flush();

        List<Product> all = productRepository.findAll();

        assertThat(all).isEmpty();
    }

    // ----- delete -----

    @Test
    @DisplayName("delete: product is no longer findable after deletion")
    void delete_existingProduct_removesFromDatabase() {
        productRepository.delete(persistedProduct);
        entityManager.flush();
        entityManager.clear();

        Optional<Product> found = productRepository.findById(persistedProduct.getProductId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("deleteById: product is no longer findable after deletion by id")
    void deleteById_existingId_removesFromDatabase() {
        productRepository.deleteById(persistedProduct.getProductId());
        entityManager.flush();
        entityManager.clear();

        Optional<Product> found = productRepository.findById(persistedProduct.getProductId());

        assertThat(found).isEmpty();
    }
}
