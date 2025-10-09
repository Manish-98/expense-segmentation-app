const Button = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  fullWidth = false,
  icon,
  ...props
}) => {
  const baseClasses = 'font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed';

  const variants = {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
    secondary: 'bg-gray-600 text-white hover:bg-gray-700 focus:ring-gray-500',
    success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    outline: 'border-2 border-gray-300 text-gray-700 hover:bg-gray-50 focus:ring-gray-500',
    ghost: 'text-gray-700 hover:bg-gray-100 focus:ring-gray-500',
    purple: 'bg-purple-600 text-white hover:bg-purple-700 focus:ring-purple-500',
    indigo: 'bg-indigo-600 text-white hover:bg-indigo-700 focus:ring-indigo-500'
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg'
  };

  const widthClass = fullWidth ? 'w-full' : '';

  return (
    <button
      className={`${baseClasses} ${variants[variant]} ${sizes[size]} ${widthClass}`}
      disabled={isLoading || props.disabled}
      {...props}
    >
      {isLoading ? (
        <div className="flex items-center justify-center">
          <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
          <span className="ml-2">Loading...</span>
        </div>
      ) : (
        <div className="flex items-center justify-center gap-2">
          {icon && <span>{icon}</span>}
          <span>{children}</span>
        </div>
      )}
    </button>
  );
};

export default Button;
