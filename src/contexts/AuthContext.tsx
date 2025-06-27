import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom'; // Optional: if context needs to navigate
import type { User, AuthResponseData } from '../types/authTypes';
import { login as apiLogin, register as apiRegister } from '../services/authService';
import type { RegisterData } from '../services/authService';
import { jwtDecode } from 'jwt-decode';
import type { JwtPayload } from 'jwt-decode';

// Extend JwtPayload if your token has custom claims like userId, roles
interface DecodedToken extends JwtPayload {
  sub: string; // Standard: subject (usually username)
  userId: number; // Custom claim we expect
  roles?: string[]; // Optional custom claim
  // iat?: number; // Standard: issued at
  // exp?: number; // Standard: expiration time
}

interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  loginUser: (credentials: { username?: string; password?: string }) => Promise<void>; // For form login
  registerUser: (data: RegisterData) => Promise<string>;
  handleOAuth2Success: (jwtToken: string) => void; // For processing token from OAuth2
  logoutUser: () => void;
  isLoading: boolean; // For initial auth state check
  decodeAndSetUser: (jwtToken: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null); // Initialized later from localStorage
  const [isLoading, setIsLoading] = useState<boolean>(true); // Start true to check localStorage
  const navigate = useNavigate(); // Optional, if context itself needs to redirect globally

  // Function to decode token, set user state, and store in localStorage
  const processAndStoreToken = useCallback((jwtToken: string) => {
    try {
      const decodedToken = jwtDecode<DecodedToken>(jwtToken);

      // Basic validation: check for expiration if 'exp' claim exists
      if (decodedToken.exp) {
        const currentTime = Date.now() / 1000; // Convert to seconds
        if (decodedToken.exp < currentTime) {
          console.warn("Token is expired.");
          logoutUser(); // Perform logout if token is expired
          return; // Stop processing
        }
      }

      const userData: User = {
        id: decodedToken.userId,
        username: decodedToken.sub,
        email: '', // Backend login response might provide this, or fetch from /users/me
        roles: decodedToken.roles || [],
        // Add firstName, lastName if available in token or fetched separately
      };

      setUser(userData);
      setToken(jwtToken);
      localStorage.setItem('authToken', jwtToken);
      localStorage.setItem('user', JSON.stringify(userData)); // Store basic user info
      console.log("Auth state updated with token. User:", userData);
    } catch (error) {
      console.error("Failed to decode token or token invalid:", error);
      logoutUser(); // Clear invalid token/user state
    }
  }, []); // Removed navigate from dependencies as logoutUser handles it

  // Check localStorage for token on initial app load
  useEffect(() => {
    setIsLoading(true);
    const storedToken = localStorage.getItem('authToken');
    if (storedToken) {
      console.log("Found stored token, processing...");
      processAndStoreToken(storedToken);
    } else {
      console.log("No stored token found.");
    }
    setIsLoading(false);
  }, [processAndStoreToken]);


  const loginUser = async (credentials: { username?: string; password?: string }) => {
    try {
      const responseData = await apiLogin(credentials);
      if (responseData.accessToken) {
        processAndStoreToken(responseData.accessToken);
        // Optionally update user email from login response if available and not in token
        if (responseData.email && user) {
            const updatedUser = { ...user, email: responseData.email };
            setUser(updatedUser);
            localStorage.setItem('user', JSON.stringify(updatedUser));
        }
      } else {
        throw new Error("Login failed: No access token received.");
      }
    } catch (error) {
      console.error("Login error in AuthContext:", error);
      logoutUser(); // Clear any partial auth state on login failure
      throw error; // Re-throw for the component to handle (e.g., show error message)
    }
  };

  const registerUser = async (data: RegisterData): Promise<string> => {
    try {
      return await apiRegister(data); // Returns success message
    } catch (error) {
      console.error("Registration error in AuthContext:", error);
      throw error; // Re-throw for the component
    }
  };

  // This function will be called by OAuth2 redirect handler or popup message handler
  const handleOAuth2Success = useCallback((jwtToken: string) => {
    console.log("Handling OAuth2 success with token in AuthContext...");
    processAndStoreToken(jwtToken);
    // Navigation to dashboard is typically handled by the component that calls this
    // (e.g., OAuth2RedirectHandlerPage or LoginPage after popup message)
  }, [processAndStoreToken]);


  const logoutUser = useCallback(() => {
    console.log("Logging out user...");
    setUser(null);
    setToken(null);
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    // navigate('/login', { replace: true }); // Let components decide navigation or do it here if always desired
  }, [/* navigate */]); // Removed navigate from dependencies if components handle redirection

  return (
    <AuthContext.Provider value={{
      isAuthenticated: !!token && !!user, // More robust check
      user,
      token,
      loginUser,
      registerUser,
      handleOAuth2Success,
      logoutUser,
      isLoading,
      decodeAndSetUser: processAndStoreToken
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};