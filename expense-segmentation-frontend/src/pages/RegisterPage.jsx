import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useAuth } from '../context/AuthContext';
import Input from '../components/Input';
import Button from '../components/Button';
import PageLayout from '../components/Layout/PageLayout';
import PageContainer from '../components/Layout/PageContainer';
import Card from '../components/Layout/Card';
import Alert from '../components/Feedback/Alert';

const schema = yup.object({
  name: yup.string().required('Name is required'),
  email: yup.string().email('Invalid email address').required('Email is required'),
  password: yup
    .string()
    .min(6, 'Password must be at least 6 characters')
    .required('Password is required'),
}).required();

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register: registerUser } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
  });

  const onSubmit = async (data) => {
    setIsLoading(true);
    setErrorMessage('');

    const result = await registerUser(data.name, data.email, data.password);

    if (result.success) {
      navigate('/dashboard');
    } else {
      setErrorMessage(result.message);
    }

    setIsLoading(false);
  };

  return (
    <PageLayout>
      <PageContainer className="flex items-center justify-center py-12">
        <div className="max-w-md w-full">
          <Card>
            <div className="mb-8">
              <h2 className="text-3xl font-bold text-center text-gray-900">
                Create Account
              </h2>
              <p className="mt-2 text-center text-sm text-gray-600">
                Sign up for a new account
              </p>
            </div>

            {errorMessage && (
              <Alert variant="error" className="mb-4">
                {errorMessage}
              </Alert>
            )}

            <form onSubmit={handleSubmit(onSubmit)}>
              <Input
                label="Full Name"
                type="text"
                placeholder="Enter your full name"
                {...register('name')}
                error={errors.name?.message}
              />

              <Input
                label="Email Address"
                type="email"
                placeholder="Enter your email"
                {...register('email')}
                error={errors.email?.message}
              />

              <Input
                label="Password"
                type="password"
                placeholder="Enter your password"
                {...register('password')}
                error={errors.password?.message}
              />

              <Button type="submit" isLoading={isLoading}>
                Sign Up
              </Button>
            </form>

            <p className="mt-6 text-center text-sm text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-blue-600 hover:text-blue-500 font-semibold">
                Sign in
              </Link>
            </p>
          </Card>
        </div>
      </PageContainer>
    </PageLayout>
  );
};

export default RegisterPage;
