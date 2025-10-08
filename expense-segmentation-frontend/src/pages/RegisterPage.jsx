import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useAuth } from '../context/AuthContext';
import Input from '../components/Input';
import Button from '../components/Button';

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
    <div className="min-h-screen bg-gray-100 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full bg-white rounded-lg shadow-md p-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-center text-gray-900">
            Create Account
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Sign up for a new account
          </p>
        </div>

        {errorMessage && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {errorMessage}
          </div>
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
      </div>
    </div>
  );
};

export default RegisterPage;
