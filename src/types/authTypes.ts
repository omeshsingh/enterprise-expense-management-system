export interface User { // Basic user info stored in context
    id: number;
    username: string;
    email: string;
    roles?: string[]; // Optional roles array
    firstName?: string;
    lastName?: string;
  }
  
  export interface AuthResponseData { // From backend login
    accessToken: string;
    tokenType: string;
    username: string;
    email: string;
    userId: number;
    // roles?: string[]; // If backend sends roles on login
  }