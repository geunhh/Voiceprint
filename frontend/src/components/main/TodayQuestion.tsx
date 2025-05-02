import QuestionCharacter from "../../assets/icons/questionCharacter.png"

interface TodayQuestionProps {
    question : string[]
}

function TodayQuestion ({question} : TodayQuestionProps) {

    return (
        <div className="flex mx-auto rounded-xl bg-yellow-50 p-4 w-11/12">
        <img src={QuestionCharacter} className="w-20 h-auto"/>
        <div className="flex-row ml-4">
            <p className="text-yellow-400 font-semibold">오늘의 질문</p>
            <p className="text-gray-500 font-medium"> {question}</p>
        </div>
        </div>
    )

}

export default TodayQuestion