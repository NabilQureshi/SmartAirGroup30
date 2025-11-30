package com.example.smartair.auth;

import com.example.smartair.models.UserRole;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class LoginPresenterTest {

    @Mock
    private LoginContract.View mockView;

    @Mock
    private AuthModel mockModel;

    private LoginPresenter presenter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        presenter = new LoginPresenter(mockView, mockModel);
    }

    @Test
    public void testOnLoginClicked_withEmptyEmail_showsError() {
        when(mockView.getEmail()).thenReturn("");
        when(mockView.getPassword()).thenReturn("password123");

        presenter.onLoginClicked();

        verify(mockView).showError("Email cannot be empty");
        verify(mockView, never()).showLoading();
    }

    @Test
    public void testOnLoginClicked_withEmptyPassword_showsError() {
        when(mockView.getEmail()).thenReturn("test@example.com");
        when(mockView.getPassword()).thenReturn("");

        presenter.onLoginClicked();

        verify(mockView).showError("Password cannot be empty");
        verify(mockView, never()).showLoading();
    }

    @Test
    public void testOnLoginClicked_withInvalidEmail_showsError() {
        when(mockView.getEmail()).thenReturn("invalidemail");
        when(mockView.getPassword()).thenReturn("password123");

        presenter.onLoginClicked();

        verify(mockView).showError("Invalid email format");
        verify(mockView, never()).showLoading();
    }

    @Test
    public void testOnLoginClicked_withValidCredentials_showsLoading() {
        when(mockView.getEmail()).thenReturn("test@example.com");
        when(mockView.getPassword()).thenReturn("password123");

        presenter.onLoginClicked();

        verify(mockView).showLoading();
        verify(mockModel).login(anyString(), anyString(), any());
    }

    @Test
    public void testOnLoginClicked_withValidCredentials_callsModel() {
        when(mockView.getEmail()).thenReturn("test@example.com");
        when(mockView.getPassword()).thenReturn("password123");

        presenter.onLoginClicked();

        verify(mockModel).login("test@example.com", "password123", presenter);
    }

    @Test
    public void testOnSuccess_hidesLoadingAndNavigatesToHome() {
        UserRole testRole = UserRole.PARENT;

        presenter.onSuccess(testRole);

        verify(mockView).hideLoading();
        verify(mockView).navigateToHome(testRole);
    }

    @Test
    public void testOnError_hidesLoadingAndShowsError() {
        String errorMessage = "Login failed";

        presenter.onError(errorMessage);

        verify(mockView).hideLoading();
        verify(mockView).showError(errorMessage);
    }

    @Test
    public void testOnDestroy_setsViewToNull() {
        presenter.onDestroy();

        presenter.onSuccess(UserRole.PARENT);

        verify(mockView, never()).hideLoading();
        verify(mockView, never()).navigateToHome(any());
    }

    @Test
    public void testOnLoginClicked_trimsEmail() {
        when(mockView.getEmail()).thenReturn("  test@example.com  ");
        when(mockView.getPassword()).thenReturn("password123");

        presenter.onLoginClicked();

        verify(mockModel).login("test@example.com", "password123", presenter);
    }

    @Test
    public void testOnLoginClicked_withShortPassword_showsError() {
        when(mockView.getEmail()).thenReturn("test@example.com");
        when(mockView.getPassword()).thenReturn("12345");

        presenter.onLoginClicked();

        verify(mockView).showError("Password must be at least 6 characters");
        verify(mockView, never()).showLoading();
    }
}