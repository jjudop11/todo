'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { apiClient, Todo } from '@/lib/api';

export default function TodosPage() {
  const [todos, setTodos] = useState<Todo[]>([]);
  const [newTodo, setNewTodo] = useState({ title: '', description: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const router = useRouter();

  useEffect(() => {
    loadTodos();
  }, []);

  const loadTodos = async () => {
    try {
      const data = await apiClient.getTodos();
      setTodos(data);
    } catch (err) {
      if (err instanceof Error && err.message.includes('401')) {
        router.push('/login');
      } else {
        setError(err instanceof Error ? err.message : 'Failed to load todos');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleCreateTodo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTodo.title.trim()) return;

    try {
      await apiClient.createTodo(newTodo);
      setNewTodo({ title: '', description: '' });
      loadTodos();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create todo');
    }
  };

  const handleToggleTodo = async (id: number, completed: boolean) => {
    try {
      await apiClient.updateTodo(id, { completed });
      loadTodos();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update todo');
    }
  };

  const handleDeleteTodo = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      await apiClient.deleteTodo(id);
      loadTodos();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete todo');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">할 일 목록</h1>
          <button
            onClick={() => router.push('/login')}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
          >
            로그아웃
          </button>
        </div>

        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md text-red-700">
            {error}
          </div>
        )}

        <form onSubmit={handleCreateTodo} className="mb-8">
          <div className="bg-white p-6 rounded-lg shadow">
            <h2 className="text-lg font-medium text-gray-900 mb-4">새 할 일 추가</h2>
            <div className="space-y-4">
              <div>
                <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                  제목
                </label>
                <input
                  id="title"
                  type="text"
                  required
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="할 일 제목"
                  value={newTodo.title}
                  onChange={(e) => setNewTodo({ ...newTodo, title: e.target.value })}
                />
              </div>
              <div>
                <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                  설명 (선택)
                </label>
                <textarea
                  id="description"
                  rows={3}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="할 일 설명"
                  value={newTodo.description}
                  onChange={(e) => setNewTodo({ ...newTodo, description: e.target.value })}
                />
              </div>
              <button
                type="submit"
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
              >
                추가
              </button>
            </div>
          </div>
        </form>

        <div className="space-y-4">
          {todos.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              할 일이 없습니다. 새로 추가해보세요!
            </div>
          ) : (
            todos.map((todo) => (
              <div key={todo.id} className="bg-white p-6 rounded-lg shadow">
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-3">
                    <input
                      type="checkbox"
                      checked={todo.completed}
                      onChange={(e) => handleToggleTodo(todo.id, e.target.checked)}
                      className="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                    />
                    <div className="flex-1">
                      <h3 className={`text-lg font-medium ${todo.completed ? 'line-through text-gray-500' : 'text-gray-900'}`}>
                        {todo.title}
                      </h3>
                      {todo.description && (
                        <p className={`mt-1 text-sm ${todo.completed ? 'text-gray-400' : 'text-gray-600'}`}>
                          {todo.description}
                        </p>
                      )}
                      <div className="mt-2 text-xs text-gray-500">
                        생성: {new Date(todo.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => handleDeleteTodo(todo.id)}
                    className="ml-4 px-3 py-1 text-sm text-red-600 hover:text-red-800"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
