package enterpriseapplication.productservice.exception;

import enterpriseapplication.productservice.api.ProductController;
import enterpriseapplication.productservice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    // ----- ResourceNotFoundException -> 404 -----

    @Test
    @DisplayName("ResourceNotFoundException on GET: response body has timestamp, message, status=404")
    void resourceNotFound_onGet_returns404BodyStructure() throws Exception {
        when(productService.getProductById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 1"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("ResourceNotFoundException on DELETE: response body has timestamp, message, status=404")
    void resourceNotFound_onDelete_returns404BodyStructure() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found with id: 5"))
                .when(productService).deleteProductById(5L);

        mockMvc.perform(delete("/api/products/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 5"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ----- MethodArgumentNotValidException -> 400 -----

    @Test
    @DisplayName("Validation: blank name returns 400 with timestamp, status, and errors map")
    void validationFailure_blankName_returns400WithErrorsMap() throws Exception {
        String json = """
                {"name": "", "unitPrice": 10.00}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.name").value("Product name is required"));
    }

    @Test
    @DisplayName("Validation: null unitPrice returns 400 with unitPrice error message")
    void validationFailure_nullUnitPrice_returns400WithUnitPriceError() throws Exception {
        String json = """
                {"name": "Valid Name"}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.unitPrice").value("Unit price is required"));
    }

    @Test
    @DisplayName("Validation: negative unitPrice returns 400 with positive constraint message")
    void validationFailure_negativeUnitPrice_returns400WithPositiveError() throws Exception {
        String json = """
                {"name": "Valid Name", "unitPrice": -5.00}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.unitPrice").value("Unit price must be positive"));
    }

    @Test
    @DisplayName("Validation: both name and unitPrice invalid returns 400 with both field errors")
    void validationFailure_multipleViolations_returns400WithMultipleErrors() throws Exception {
        String json = """
                {"name": "", "unitPrice": -1.00}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.unitPrice").exists());
    }
}
