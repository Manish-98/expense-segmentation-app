import { useState } from 'react';
import helpTexts from '../helpTexts.json';

const HelpTooltip = ({ type, className = "" }) => {
  const [showTooltip, setShowTooltip] = useState(false);
  
  const helpText = helpTexts[type];
  if (!helpText) return null;

  return (
    <div className={`relative inline-block ${className}`}>
      <button
        onMouseEnter={() => setShowTooltip(true)}
        onMouseLeave={() => setShowTooltip(false)}
        onClick={() => setShowTooltip(!showTooltip)}
        className="text-gray-400 hover:text-gray-600 ml-1 focus:outline-none focus:ring-2 focus:ring-blue-500 rounded-full p-1"
        title={`Help: ${helpText.title}`}
      >
        <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-8-3a1 1 0 00-.867.5 1 1 0 11-1.731-1A3 3 0 0113 8a3.001 3.001 0 01-2 2.83V11a1 1 0 11-2 0v-1a1 1 0 011-1 1 1 0 100-2zm0 8a1 1 0 100-2 1 1 0 000 2z" clipRule="evenodd" />
        </svg>
      </button>
      
      {showTooltip && (
        <div className="absolute z-50 w-64 p-3 bg-gray-800 text-white text-sm rounded-lg shadow-lg border border-gray-700">
          <div className="font-semibold text-blue-300 mb-1">{helpText.title}</div>
          <div className="text-gray-200 leading-relaxed">{helpText.description}</div>
          <div className="absolute -top-1 left-4 w-2 h-2 bg-gray-800 transform rotate-45 border-l border-t border-gray-700"></div>
        </div>
      )}
    </div>
  );
};

export default HelpTooltip;