const EmptyState = ({ icon, title, description, action }) => {
  return (
    <div className="text-center py-12">
      {icon && <div className="text-6xl mb-4">{icon}</div>}
      <h3 className="text-lg font-medium text-gray-900 mb-2">{title}</h3>
      {description && <p className="text-gray-600 mb-6">{description}</p>}
      {action && <div>{action}</div>}
    </div>
  );
};

export default EmptyState;
