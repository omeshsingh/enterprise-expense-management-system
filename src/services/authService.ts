import apiClient from './api';
import type { AuthResponseData, User } from '../types/authTypes';

interface LoginCredentials {
  username?: string;
  email?: string;
  password?: string;
}

export interface RegisterData {
    username?: string;
    email?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
}


export const login = async (credentials: LoginCredentials): Promise<AuthResponseData> => {
  const payload = {
    username: credentials.username || credentials.email,
    password: credentials.password
  };
  const response = await apiClient.post<AuthResponseData>('/auth/login', payload);
  return response.data;
};

export const register = async (userData: RegisterData): Promise<string> => { // Assuming backend returns string message
  const response = await apiClient.post<string>('/auth/register', userData);
  return response.data;
};