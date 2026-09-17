package org.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CheckPoint 5 - Login")
public class Login {

    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String USUARIO_VALIDO = "standard_user";
    private static final String SENHA_VALIDA = "secret_sauce";
    private static final String USUARIO_BLOQUEADO = "locked_out_user"; // conta de teste do saucedemo p/ simular 403
    private static final String SENHA_INVALIDA = "senha_incorreta_123";

    private static final By CAMPO_USUARIO = By.id("user-name");
    private static final By CAMPO_SENHA = By.id("password");
    private static final By BOTAO_LOGIN = By.id("login-button");
    private static final By ICONE_CARRINHO = By.id("shopping_cart_container");
    private static final By MENSAGEM_ERRO = By.cssSelector("[data-test='error']");

    private WebDriver driver;

    @BeforeEach
    void abrirNavegador() {
        driver = new ChromeDriver();
    }

    @AfterEach
    void fecharNavegador() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("CT1 - Login com sucesso")
    void deveLogarComCredenciaisValidas() {
        driver.get(BASE_URL);
        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Swag Labs", driver.getTitle());

        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        driver.findElement(BOTAO_LOGIN).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        assertEquals(BASE_URL + "inventory.html", driver.getCurrentUrl());
        assertTrue(driver.findElement(ICONE_CARRINHO).isDisplayed());
    }

    @Test
    @DisplayName("CT2 - Campo usuário vazio (400)")
    void deveExibirErroQuandoUsuarioVazio() {
        driver.get(BASE_URL);

        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        driver.findElement(BOTAO_LOGIN).click();

        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Epic sadface: Username is required", driver.findElement(MENSAGEM_ERRO).getText());
    }

    @Test
    @DisplayName("CT3 - Campo senha vazio (400)")
    void deveExibirErroQuandoSenhaVazia() {
        driver.get(BASE_URL);

        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);

        driver.findElement(BOTAO_LOGIN).click();

        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Epic sadface: Password is required", driver.findElement(MENSAGEM_ERRO).getText());
    }

    @Test
    @DisplayName("CT4 - Credenciais inválidas (401)")
    void deveExibirErroQuandoCredenciaisInvalidas() {
        driver.get(BASE_URL);

        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_INVALIDA);

        driver.findElement(BOTAO_LOGIN).click();

        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Epic sadface: Username and password do not match any user in this service",
                driver.findElement(MENSAGEM_ERRO).getText());
    }

    @Test
    @DisplayName("CT5 - Conta bloqueada (403)")
    void deveExibirErroQuandoContaBloqueada() {
        driver.get(BASE_URL);

        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_BLOQUEADO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        driver.findElement(BOTAO_LOGIN).click();

        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Epic sadface: Sorry, this user has been locked out.",
                driver.findElement(MENSAGEM_ERRO).getText());
    }
}