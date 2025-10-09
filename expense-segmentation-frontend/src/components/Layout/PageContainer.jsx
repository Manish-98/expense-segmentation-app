const PageContainer = ({ children, className = '' }) => {
  return (
    <main className={`max-w-7xl mx-auto py-6 sm:px-6 lg:px-8 ${className}`}>
      <div className="px-4 py-6 sm:px-0">
        {children}
      </div>
    </main>
  );
};

export default PageContainer;
