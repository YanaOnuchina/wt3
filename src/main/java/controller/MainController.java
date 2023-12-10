package controller;
import data_access.ResearchDao;
import data_access.UserDao;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import service.ResearchInfo;
import service.User;
import service.UserDataChecker;
import service.UserIdentifier;
import java.util.HashMap;
import java.util.Map;


@Controller
public class MainController {

    @Autowired
    UserDao userDao;

    @Autowired
    ResearchDao researchDao;

    @RequestMapping("/signin")
    public ModelAndView showSigninForm(){
        return new ModelAndView("signin");
    }

    @RequestMapping("/signin-check")
    public ModelAndView checkSigninData(HttpServletRequest request, HttpServletResponse response) {
        User user = userDao.getUserByEmail((String) request.getParameter("email"));
        if (user.getPassword().equals(request.getParameter("psw"))){ //user exist and password correct
            userDao.updateStatus(user.getId(), "active");
            Cookie cookie = new Cookie("usercode", String.valueOf(user.getId()));
            response.addCookie(cookie);
            Cookie cookie2 = new Cookie("userrole", user.getRole());
            response.addCookie(cookie2);
            String[] researches;
            if (user.getRole().equals("admin")){ //admin
                researches = researchDao.getAllResearches().toArray(new String[0]);
            }
            else { //user
                researches = researchDao.getActiveForUser(user.getId()).toArray(new String[0]);
            }
            request.setAttribute("user_role", user.getRole());
            return new ModelAndView("research_list", "researches", researches);
        }
        else if (user.getEmail().equals("")) { //user have no account
            return new ModelAndView("signin", "code", 1);
        }
        return new ModelAndView("signin", "code", 2); //incorrect password
    }

    @RequestMapping("/registration")
    public ModelAndView showRegisterForm() {
        return new ModelAndView("index");
    }

    @RequestMapping("/registration-check")
    public ModelAndView checkRegistrationData(HttpServletRequest request){
        String email = request.getParameter("email");
        String password = request.getParameter("psw");
        String passwordRepeat = request.getParameter("psw-repeat");
        int checkResult = UserDataChecker.checkData(email, password, passwordRepeat);
        if (checkResult == 0) { //enter correct data
            User user = userDao.getUserByEmail(email);
            if (user.getEmail().equals("")) { //new user
                return new ModelAndView("signin");
            }
            else { //user with account
                userDao.addUser(email, password);
                return new ModelAndView("index", "code", 4);
            }
        }
        else { //enter incorrect data
            return new ModelAndView("index", "code", checkResult);
        }
    }

    @RequestMapping("/add-research-page")
    public ModelAndView showAddResearchPage(){
        return new ModelAndView("add_research");
    }

    @RequestMapping("/add-research")
    public ModelAndView addResearch(HttpServletRequest request){
        String name = request.getParameter("name");
        researchDao.addResearch(name);
        return new ModelAndView("add_research");
    }

    @RequestMapping("/list")
    public ModelAndView showResearchInfo(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")) { //Cookies expired
            return new ModelAndView("signin");
        }
        else {
            String[] researches;
            if (UserIdentifier.getRole(cookies).equals("admin")) { //admin
                researches = researchDao.getAllResearches().toArray(new String[0]);
            } else { //user
                researches = researchDao.getActiveForUser(user_id).toArray(new String[0]);
            }
            return new ModelAndView("research_list", "researches", researches);
        }
    }

    @RequestMapping("/statistics")
    public ModelAndView showStatistics(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")){ //Cookies expired
            return new ModelAndView("signin");
        }
        else {
            int research_count = researchDao.countResearches(user_id);
            int question_count = researchDao.countQuestions(user_id);
            HashMap<String, Integer> topics_count = researchDao.countTopics(user_id);
            request.setAttribute("research_count", research_count);
            request.setAttribute("question_count", question_count);
            request.setAttribute("topics_count", topics_count);
            return new ModelAndView("statistics");
        }
    }

    @RequestMapping("/research_info")
    public ModelAndView getResearchInfo(HttpServletRequest request){
        String research_name = request.getParameter("item");
        ResearchInfo researchInfo = researchDao.getResearchInfo(research_name);
        String[] topics = researchDao.getTopics().keySet().toArray(new String[0]);
        request.setAttribute("topics", topics);
        request.setAttribute("research_name", researchInfo.getResearch_name());
        request.setAttribute("questions", researchInfo.getQuestions());
        request.setAttribute("status", researchInfo.getStatus());
        return new ModelAndView("research_management");
    }

    @RequestMapping("/add-question")
    public ModelAndView addQuestionToResearch(HttpServletRequest request){
        String research_name = request.getParameter("research");
        String topic = request.getParameter("topic");
        String question = request.getParameter("question");
        researchDao.addQuestion(research_name, topic, question);
        ResearchInfo researchInfo = researchDao.getResearchInfo(research_name);
        String[] topics = researchDao.getTopics().keySet().toArray(new String[0]);
        request.setAttribute("topics", topics);
        request.setAttribute("research_name", researchInfo.getResearch_name());
        request.setAttribute("questions", researchInfo.getQuestions());
        request.setAttribute("status", researchInfo.getStatus());
        return new ModelAndView("research_management");
    }

    @RequestMapping("/change-status")
    public ModelAndView changeResearchStatus(HttpServletRequest request){
        String changedResearch = request.getParameter("changed_research");
        String status = request.getParameter("research_status");
        researchDao.setResearchStatus(changedResearch, status);
        ResearchInfo researchInfo = researchDao.getResearchInfo(changedResearch);
        String[] topics = researchDao.getTopics().keySet().toArray(new String[0]);
        request.setAttribute("topics", topics);
        request.setAttribute("research_name", researchInfo.getResearch_name());
        request.setAttribute("questions", researchInfo.getQuestions());
        request.setAttribute("status", researchInfo.getStatus());
        return new ModelAndView("research_management");
    }

    @RequestMapping("/question_info")
    public ModelAndView showQuestionInfo(HttpServletRequest request){
        String question = request.getParameter("item");
        HashMap<String, String> answers = researchDao.getAnswers(question);
        request.setAttribute("question", question);
        request.setAttribute("answers", answers);
        request.setAttribute("research", request.getParameter("research"));
        return new ModelAndView("question_info");
    }

    @RequestMapping("/take_research")
    public ModelAndView takeResearch(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")){ //Cookies expired
            return new ModelAndView("signin");
        }
        ResearchInfo researchInfo = researchDao.getResearchInfo(request.getParameter("item"));
        request.setAttribute("research_name", researchInfo.getResearch_name());
        request.setAttribute("questions", researchInfo.getQuestions());
        return new ModelAndView("take_research");
    }

    @RequestMapping("/post_answers")
    public ModelAndView postResearchAnswers(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")){ //Cookies expired
            return new ModelAndView("signin");
        }
        Map<String, String[]> requestParams = request.getParameterMap();
        String research_name = requestParams.get("research")[0];
        requestParams.forEach((key, value) -> {
            if (!key.equals("research")) {
                    researchDao.addAnswer(research_name, key, value[0], user_id);
            }
        });
        String[] researches = researchDao.getActiveForUser(user_id).toArray(new String[0]);
        return new ModelAndView("research_list", "researches", researches);
    }

    @RequestMapping("/account")
    public ModelAndView showAccountPage(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")){ //Cookies expired
            return new ModelAndView("signin");
        }
        User user = userDao.getUserByEmail(userDao.getLoginById(user_id));
        request.setAttribute("login", user.getEmail());
        request.setAttribute("password", user.getPassword());
        return new ModelAndView("account_info");
    }

    @RequestMapping("/signout")
    public ModelAndView signOut(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")){ //Cookies expired
            return new ModelAndView("signin");
        }
        userDao.updateStatus(user_id, "inactive");
        Cookie userCodeRemove = new Cookie("usercode", "0");
        userCodeRemove.setMaxAge(0);
        response.addCookie(userCodeRemove);
        Cookie userRoleRemove = new Cookie("userrole", "");
        userRoleRemove.setMaxAge(0);
        response.addCookie(userRoleRemove);
        return new ModelAndView("signin");
    }

    @RequestMapping("/change_data")
    public ModelAndView changeRegisterData(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        int user_id = UserIdentifier.getUsercode(cookies);
        if (user_id == 0 || UserIdentifier.getRole(cookies).equals("")){ //Cookies expired
            return new ModelAndView("signin");
        }
        String email = request.getParameter("email");
        String password = request.getParameter("psw");
        String passwordRepeat = request.getParameter("psw-repeat");
        int code = UserDataChecker.checkData(email, password, passwordRepeat);
        if (code == 0) {
            User user = userDao.getUserByEmail(email);
            if (user.getId() == 0 || user.getId() == user_id) {
                userDao.updateUserInfo(user_id, email, password);
                user = userDao.getUserByEmail(userDao.getLoginById(user_id));
                request.setAttribute("login", user.getEmail());
                request.setAttribute("password", user.getPassword());
            }
            else {
                request.setAttribute("login", email);
                request.setAttribute("password", password);
                request.setAttribute("code", 4);
            }
            return new ModelAndView("account_info");
        }
        else {
            User user = userDao.getUserByEmail(userDao.getLoginById(user_id));
            request.setAttribute("login", user.getEmail());
            request.setAttribute("password", user.getPassword());
            request.setAttribute("code", code);
            return new ModelAndView("account_info");
        }
    }

}
