import Link from 'next/link';

export default function HomePage() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-md w-full space-y-8 text-center">
        <div>
          <h1 className="text-4xl font-bold text-gray-900 mb-4">Todo App</h1>
          <p className="text-gray-600 mb-8">할 일을 효율적으로 관리하세요</p>
        </div>
        
        <div className="space-y-4">
          <Link
            href="/login"
            className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            로그인
          </Link>
          
          <Link
            href="/register"
            className="w-full flex justify-center py-3 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            회원가입
          </Link>
        </div>
        
        <div className="mt-8 text-sm text-gray-500">
          <p>API 문서: <a href="http://localhost:8080/swagger-ui/index.html" target="_blank" rel="noopener noreferrer" className="text-indigo-600 hover:text-indigo-500">Swagger UI</a></p>
        </div>
      </div>
    </div>
  );
}