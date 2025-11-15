export interface User {
  id: number;
  email: string;
  username?: string;
  firstName: string;
  lastName: string;
  fullName?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  phoneNumber?: string;
  profilePictureUrl?: string;
  bio?: string;
  city?: string;
  fitnessLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  preferredDistanceUnit?: 'KILOMETERS' | 'MILES';
  isPublicProfile?: boolean;
  role: 'USER' | 'ADMIN' | 'MODERATOR';
  createdAt: string;
  updatedAt: string;
}

export interface UserRegistration {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  phoneNumber?: string;
  bio?: string;
  city?: string;
  fitnessLevel?: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
  preferredDistanceUnit?: 'KILOMETERS' | 'MILES';
  isPublicProfile?: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: User;
  expiresIn: number;
}

export interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordReset {
  token: string;
  newPassword: string;
}