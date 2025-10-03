const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  name: string;
}

export interface Todo {
  id: number;
  title: string;
  description?: string;
  completed: boolean;
  priority?: string;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTodoRequest {
  title: string;
  description?: string;
}

export interface UpdateTodoRequest {
  title?: string;
  description?: string;
  completed?: boolean;
}

class ApiClient {
  private baseUrl: string;

  constructor() {
    this.baseUrl = API_URL;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const response = await fetch(url, {
      ...options,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Unknown error' }));
      throw new Error(error.message || 'Request failed');
    }

    return response.json();
  }

  // Auth endpoints
  async login(data: LoginRequest) {
    return this.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async register(data: RegisterRequest) {
    return this.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async refresh(data: { username: string; refreshToken: string }) {
    return this.request('/api/auth/refresh', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  // Todo endpoints
  async getTodos(params?: {
    completed?: boolean;
    priority?: string;
    tag?: string;
    page?: number;
    size?: number;
  }) {
    const searchParams = new URLSearchParams();
    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined) {
          searchParams.append(key, value.toString());
        }
      });
    }
    const query = searchParams.toString();
    return this.request<Todo[]>(`/api/todos${query ? `?${query}` : ''}`);
  }

  async createTodo(data: CreateTodoRequest) {
    return this.request('/api/todos', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async updateTodo(id: number, data: UpdateTodoRequest) {
    return this.request(`/api/todos/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  async deleteTodo(id: number) {
    return this.request(`/api/todos/${id}`, {
      method: 'DELETE',
    });
  }
}

export const apiClient = new ApiClient();
