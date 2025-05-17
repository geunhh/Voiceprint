import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const LoginSuccess = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get('access');

    if (accessToken) {
      localStorage.setItem('accessToken', accessToken);
      console.log('AccessToken 저장 완료:', accessToken);
      navigate('/');
    } else {
      console.error('access 파라미터 없음');
      navigate('/login');
    }
  }, [navigate]);

  return (
    <div>
      <h2>로그인 중입니다...</h2>
    </div>
  );
};

export default LoginSuccess;
