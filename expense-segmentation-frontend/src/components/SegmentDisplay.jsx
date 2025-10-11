import { useState } from 'react';
import HelpTooltip from './HelpTooltip';

const SegmentDisplay = ({ 
  segment, 
  isEditing, 
  onEdit, 
  onSave, 
  onCancel, 
  onDelete, 
  onInputChange,
  expenseAmount,
  categories = [],
  canModify = true
}) => {
  const [editForm, setEditForm] = useState({
    category: segment.category,
    amount: segment.amount
  });

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }));
    onInputChange(segment.id, e);
  };

  const handleSave = () => {
    onSave(segment.id, editForm);
  };

  const calculatePercentage = (amount) => {
    if (!amount || !expenseAmount) return '0.00';
    const percentage = (parseFloat(amount) / parseFloat(expenseAmount)) * 100;
    return percentage.toFixed(2);
  };

  if (isEditing) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-end">
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700 flex items-center">
            Category
            <HelpTooltip type="category" />
          </label>
          <select
            name="category"
            value={editForm.category}
            onChange={handleInputChange}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          >
            <option value="">Select...</option>
            {categories.map(category => (
              <option key={category} value={category}>{category}</option>
            ))}
          </select>
        </div>
        
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700 flex items-center">
            Amount
            <HelpTooltip type="amount" />
          </label>
          <input
            type="number"
            name="amount"
            value={editForm.amount}
            onChange={handleInputChange}
            placeholder="0.00"
            min="0.01"
            max={expenseAmount}
            step="0.01"
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
        
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700 flex items-center">
            Percentage
            <HelpTooltip type="percentage" />
          </label>
          <div className="px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg text-gray-900 h-10 flex items-center">
            {calculatePercentage(editForm.amount)}%
          </div>
        </div>
        
        <div className="flex space-x-2">
          <button
            onClick={handleSave}
            className="px-3 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700"
          >
            Save
          </button>
          <button
            onClick={onCancel}
            className="px-3 py-2 bg-gray-600 text-white text-sm font-medium rounded-lg hover:bg-gray-700"
          >
            Cancel
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-center">
      <div className="px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg">
        <div className="text-sm text-gray-500">Category</div>
        <div className="font-medium text-gray-900">{segment.category}</div>
      </div>
      
      <div className="px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg">
        <div className="text-sm text-gray-500">Amount</div>
        <div className="font-medium text-gray-900">${parseFloat(segment.amount).toFixed(2)}</div>
      </div>
      
      <div className="px-3 py-2 bg-gray-50 border border-gray-300 rounded-lg">
        <div className="text-sm text-gray-500">Percentage</div>
        <div className="font-medium text-gray-900">{segment.percentage}%</div>
      </div>
      
      {canModify && (
        <div className="flex space-x-2">
          <button
            onClick={() => onEdit(segment.id)}
            className="text-blue-600 hover:text-blue-800 text-sm font-medium"
            title="Edit segment"
          >
            ✏️ Edit
          </button>
          <button
            onClick={() => onDelete(segment.id)}
            className="text-red-600 hover:text-red-800 text-sm font-medium"
            title="Delete segment"
          >
            🗑️ Delete
          </button>
        </div>
      )}
    </div>
  );
};

export default SegmentDisplay;