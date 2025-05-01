import { useNavigate } from "react-router";
import {
  create,
  group_default,
  group_selected,
  main_default,
  main_selected,
  my_default,
  my_selected,
  temp_default,
  temp_selected,
} from "../../assets/icons";

interface TabbarProps {
  type: "Main" | "Temp" | "Create" | "Group" | "My";
  onClick: () => void;
  name?: string;
}

const Tabbar = ({ type: currentType }: TabbarProps) => {
  const navigate = useNavigate();

  const tabItems: TabbarProps[] = [
    { type: "Main", onClick: () => navigate("/main"), name: "메인" },
    { type: "Temp", onClick: () => navigate("/diary/temp"), name: "임시" },
    { type: "Create", onClick: () => navigate("/diary/setting/friend") },
    { type: "Group", onClick: () => navigate("/group"), name: "그룹" },
    { type: "My", onClick: () => navigate("/my"), name: "마이" },
  ];

  const getIconSource = (itemType: TabbarProps["type"]) => {
    const isSelected = itemType === currentType;

    switch (itemType) {
      case "Main":
        return isSelected ? main_selected : main_default;
      case "Temp":
        return isSelected ? temp_selected : temp_default;
      case "Create":
        return create;
      case "Group":
        return isSelected ? group_selected : group_default;
      case "My":
        return isSelected ? my_selected : my_default;
      default:
        return "";
    }
  };

  return (
    <div className="flex justify-around items-center h-16 border border-gray-100 border-opacity-60 bg-white">
      {tabItems.map((item) => (
        <div
          key={item.type}
          className={`flex flex-col items-center justify-center flex-1 ${item.type === currentType ? "text-black" : "text-gray-400"}`}
          onClick={item.onClick}
        >
          <div
            className={`${item.type === "Create" ? "flex items-center justify-center -mt-16" : ""}`}
          >
            <img
              src={getIconSource(item.type)}
              alt={item.name}
              className={`${item.type === "Create" ? "w-20 h-20" : "w-6 h-6"}`}
            />
          </div>
          {item.name && item.type !== "Create" && (
            <div className="text-xs mt-1">{item.name}</div>
          )}
        </div>
      ))}
    </div>
  );
};

export default Tabbar;
