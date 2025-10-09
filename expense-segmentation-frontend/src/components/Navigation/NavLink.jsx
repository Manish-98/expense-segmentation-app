import { Link, useLocation } from 'react-router-dom';

const NavLink = ({ to, children, className = '' }) => {
  const location = useLocation();
  const isActive = location.pathname === to;

  const activeClass = isActive
    ? 'text-blue-600 hover:text-blue-800'
    : 'text-gray-700 hover:text-gray-900';

  return (
    <Link
      to={to}
      className={`font-medium transition-colors ${activeClass} ${className}`}
    >
      {children}
    </Link>
  );
};

export default NavLink;
