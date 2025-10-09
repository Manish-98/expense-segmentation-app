const Card = ({ children, className = '', title, actions }) => {
  return (
    <div className={`bg-white rounded-lg shadow ${className}`}>
      {title && (
        <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
          <h2 className="text-2xl font-bold text-gray-900">{title}</h2>
          {actions && <div className="flex gap-2">{actions}</div>}
        </div>
      )}
      <div className="p-6">
        {children}
      </div>
    </div>
  );
};

export default Card;
