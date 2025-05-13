import { useLocation, useNavigate } from 'react-router-dom';
import backIcon from "../../assets/icons/backIcon.png";
import '../../index.css';

const Appbar = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const hidePages = ['/', '/main'];
    const shouldHide = hidePages.includes(location.pathname)

    if (shouldHide) return null

    return (
        <div className='w-full h-14 flex items-center bg-white'>
            <button onClick={() => navigate(-1)}>
                <img src={backIcon} alt="뒤로가기" className='w-10'/>
            </button>

        </div>
    )

}

export default Appbar