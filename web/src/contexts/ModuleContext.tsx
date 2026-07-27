import React, { createContext, useContext, useState, ReactNode } from 'react';

// Define the shape of the context
interface ModuleContextType {
  selectedModule: string;
  setSelectedModule: (module: string) => void;
}

// Create the context
const ModuleContext = createContext<ModuleContextType | undefined>(undefined);

// Provider component
export const ModuleProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [selectedModule, setSelectedModule] = useState<string>('Asset Management');

  return (
    <ModuleContext.Provider value={{ selectedModule, setSelectedModule }}>
      {children}
    </ModuleContext.Provider>
  );
};

// Hook to use the context
export const useModuleContext = (): ModuleContextType => {
  const context = useContext(ModuleContext);
  if (!context) {
    throw new Error('useModuleContext must be used within a ModuleProvider');
  }
  return context;
};
