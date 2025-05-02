import { useNavigate } from "react-router-dom";
import "../../index.css";

interface UserProfileProps {
    userId:number;
    userName:string;
    userImage:string;
    customThemaId:number|null; // 커스텀 테마 Id
}

function UserProfile (props:UserProfileProps) {

    const {userName, userImage} = props
    const navigate = useNavigate()
    const handleClick = () => {
        navigate('/my/edit');
      };
    
    return (
        <div className="flex bg-yellow-50 p-2 rounded-2xl gap-2">
            <img src={userImage} className="rounded-full w-20"/>
            <div className="flex-row self-center">
                <p className="text-xl font-semibold text-gray-700">{userName}</p>
                <p className="text-gray-500" onClick={handleClick}>회원 정보 수정하기</p>
            </div> 
        </div>
    )
}

export default UserProfile