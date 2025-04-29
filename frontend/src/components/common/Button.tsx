import '../../index.css'

interface ButtonProps {
    text : string
    type : "fill" | "line"
    size : "M" | "L"
    onClick : () => void
}

const Button = ({text, type,size, onClick} : ButtonProps) => {

    const baseStyle = "rounded-xl"

    const typeStyle = type==='fill' ? "bg-yellow-500 text-white":"border-2 border-yellow-500 text-yellow-500"

    const sizeStyle = size === "M" ? "h-11 w-28 text-sm" : "h-11 w-64 text-base"


    return (
        <button
            type="button"
            onClick={onClick}
            className={`${baseStyle} ${typeStyle} ${sizeStyle}`}
            >
            {text}
        </button>
    )
}

export default Button