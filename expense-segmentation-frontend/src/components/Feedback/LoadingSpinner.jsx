const LoadingSpinner = ({ size = 'md', fullScreen = false }) => {
  const sizeClasses = {
    sm: 'w-4 h-4',
    md: 'w-12 h-12',
    lg: 'w-16 h-16'
  };

  const spinner = (
    <div className={`${sizeClasses[size]} border-4 border-blue-600 border-t-transparent rounded-full animate-spin`}></div>
  );

  if (fullScreen) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        {spinner}
      </div>
    );
  }

  return spinner;
};

export default LoadingSpinner;
