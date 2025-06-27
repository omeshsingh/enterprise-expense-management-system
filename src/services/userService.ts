import apiClient from './api';
import type { User } from '../types/authTypes'; // Assuming User type for response from /me
// Or define a more specific UserProfileDto if needed

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
}

// Assuming UserResponseDto from backend matches User type in frontend for simplicity
// or create a specific UserProfileResponseDto matching backend's UserResponseDto
export const getCurrentUserProfile = async (): Promise<User> => { // Adjust return type if needed
  const response = await apiClient.get<User>('/users/me');
  return response.data;
};

export const updateUserProfile = async (data: UserUpdateRequest): Promise<User> => { // Adjust return type
  const response = await apiClient.put<User>('/users/me', data);
  return response.data;
};