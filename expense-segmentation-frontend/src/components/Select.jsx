import { forwardRef } from 'react';

const Select = forwardRef(({ label, error, options, ...props }, ref) => {
  return (
    <div className="mb-4">
      {label && (
        <label className="block text-gray-700 text-sm font-semibold mb-2">
          {label}
        </label>
      )}
      <select
        ref={ref}
        className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 ${
          error ? 'border-red-500' : 'border-gray-300'
        }`}
        {...props}
      >
        <option value="">Select...</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <p className="text-red-500 text-xs mt-1">{error}</p>}
    </div>
  );
});

Select.displayName = 'Select';

export default Select;
