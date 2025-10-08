const Input = ({ label, error, ...props }) => {
  return (
    <div className="mb-4">
      {label && (
        <label className="block text-gray-700 text-sm font-semibold mb-2">
          {label}
        </label>
      )}
      <input
        className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        {...props}
      />
      {error && (
        <p className="text-red-500 text-xs mt-1">{error}</p>
      )}
    </div>
  );
};

export default Input;
