package service;

public class User {

    private int id;
    private String email;
    private String password;
    private String role;
    private String status;

    public User() {
        setId(0);
        setEmail("");
        setPassword("");
        setRole("");
        setStatus("");
    }

    public User(int id, String email, String password, String role, String status) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
