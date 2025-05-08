import React from "react";

interface PageTitleProps {
  title: string;
  subtitle: string;
}

const PageTitle: React.FC<PageTitleProps> = ({ title, subtitle }) => {
  return (
    <div className="text-left mt-10 mb-4 pl-6">
      <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
      <p className="text-lg text-gray-500">{subtitle}</p>
    </div>
  );
};

export default PageTitle;
