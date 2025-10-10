

const SegmentForm = ({ 
  segment, 
  index, 
  isLast, 
  canRemove, 
  onInputChange, 
  onAddRow, 
  onRemoveRow,
  expenseAmount 
}) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
      <div className="space-y-1">
        <label className="block text-sm font-medium text-gray-700">
          Category
        </label>
        <select
          name="category"
          value={segment.category}
          onChange={(e) => onInputChange(index, e)}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          required
        >
          <option value="">Select...</option>
          <option value="Travel">Travel</option>
          <option value="Meals">Meals</option>
          <option value="Supplies">Supplies</option>
          <option value="Entertainment">Entertainment</option>
          <option value="Office">Office</option>
          <option value="Training">Training</option>
          <option value="Other">Other</option>
        </select>
      </div>
      
      <div className="space-y-1">
        <label className="block text-sm font-medium text-gray-700">
          Amount
        </label>
        <input
          type="number"
          name="amount"
          value={segment.amount}
          onChange={(e) => onInputChange(index, e)}
          placeholder="0.00"
          min="0.01"
          max={expenseAmount}
          step="0.01"
          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          required
        />
      </div>
      
      <div className="space-y-1">
        <label className="block text-sm font-medium text-gray-700">
          Percentage
        </label>
        <div className="px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 h-10 flex items-center">
          {segment.percentage ? `${segment.percentage}%` : 'Auto-calculated'}
        </div>
      </div>
      
      <div className="flex space-x-2">
        <div className="h-6"></div>
        <div className="flex space-x-2">
          {canRemove && (
            <button
              onClick={() => onRemoveRow(index)}
              className="text-red-600 hover:text-red-800 text-sm font-medium"
            >
              Remove
            </button>
          )}
          {isLast && (
            <button
              onClick={onAddRow}
              className="text-blue-600 hover:text-blue-800 text-sm font-medium"
            >
              Add Segment
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default SegmentForm;