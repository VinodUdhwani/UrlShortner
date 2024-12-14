package in.co.codeplanet.UrlShortner.Controller;

import in.co.codeplanet.UrlShortner.bean.EmailDetails;
import in.co.codeplanet.UrlShortner.bean.User;
import in.co.codeplanet.UrlShortner.service.EmailService;
import in.co.codeplanet.UrlShortner.utility.Otp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@RestController
public class UrlController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("signup")
    public String signUp(@RequestBody User user) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "select * from user where username=? or email=?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return "username or email already exist";
            } else {
                String otp = Otp.generateOtp(4);
                emailService.sendMail(new EmailDetails(user.getEmail(), "Otp Verification", "your verification otp is " + otp));
                String query = "insert into user(username,email,password,otp,is_verified)values(?,?,?,?,?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getEmail());
                preparedStatement.setString(3, user.getPassword());
                preparedStatement.setString(4, otp);
                preparedStatement.setInt(5, 0);
                preparedStatement.executeUpdate();
                return "your account has been created successfully ";
            }
        } catch (Exception e) {
            return "Something went wrong.try again";
        }
    }

    @PostMapping("verify")
    public String verification(@RequestBody User user) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String query = "select otp from user where email=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                if (resultSet.getString(1).equals(user.getOtp())) {
                    String sql = "update user set is_verified=1 where email=?";
                    PreparedStatement preparedStatement1 = connection.prepareStatement(sql);
                    preparedStatement1.setString(1, user.getEmail());
                    preparedStatement1.executeUpdate();
                    return "your account has been verified successfully";
                } else {
                    return "Invalid otp";
                }
            } else {
                return "Email does not exist.";
            }
        } catch (Exception e) {
            return "Something went wrong.";
        }
    }


    @PostMapping("login")
    public String login(@RequestBody User user) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String query = "select * from user where email=? and password=? and is_verified=1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getPassword());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return "Login Successfully";
            } else {
                return "Invalid email or password or email not verified";
            }
        } catch (Exception e) {
            return "Something went wrong.";
        }
    }

    @GetMapping("forgetpassword")
    public String forgetPassword(@RequestParam String username) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            String query = "select email from user where username=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                String password = Otp.generateOtp(8);
                EmailDetails emailDetails = new EmailDetails(resultSet.getString(1), "Password", "your login passowrd is " + password + " you can change your passowrd for security purpose.");
                emailService.sendMail(emailDetails);
                String sql = "update user set password=? where username=?";
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql);
                preparedStatement1.setString(1, password);
                preparedStatement1.setString(2, username);
                preparedStatement1.executeUpdate();
                return "your new password has been sent over your email.";
            }
            else {
                return "username does not exist";
            }
        }
        catch(Exception e){
            return "Something went wrong.try again.";
        }
    }

    @PostMapping("changepassword")
    public String changePassword(@RequestBody User user){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select * from user where email=? and password=? and is_verified=1";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setString(1,user.getEmail());
            preparedStatement.setString(2,user.getPassword());
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                String sql="update user set password=? where email=?";
                PreparedStatement preparedStatement1= connection.prepareStatement(sql);
                preparedStatement1.setString(1,user.getNewPassword());
                preparedStatement1.setString(2, user.getEmail());
                preparedStatement1.executeUpdate();
                return "your password has been changed successfully";
            }
            else{
                return "Invalid email or password or email not verified";
            }
        }
        catch (Exception e){
            return "Something went wrong.try again";
        }
    }


    @GetMapping("urlshortner")
    public String urlShortner(@RequestParam String long_url,String short_url,Integer userId){
        if(userId==null)
            userId=0;
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String sql="select long_url from url where short_url=?";
            PreparedStatement preparedStatement= connection.prepareStatement(sql);
            preparedStatement.setString(1,"urlshortner.cc/"+ short_url);
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return "That custom hash is already in use";
            }
            else {
                String query = "insert into url values(?,?,?)";
                PreparedStatement preparedStatement1 = connection.prepareStatement(query);
                preparedStatement1.setString(1, long_url);
                preparedStatement1.setString(2, "urlshortner.cc/"+short_url);
                preparedStatement1.setInt(3, userId);
                preparedStatement1.executeUpdate();
                return "short url generated successfully";
            }
        }
        catch (Exception e){
            return "Something went wrong.";
        }
    }


    @GetMapping("shorturl")
    public String getLongUrl(@RequestParam String short_url){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select long_url from url where short_url=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setString(1,"urlshortner.cc/"+short_url);
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString(1);
            }
            else {
                return "no long url linked corresponding to this short url";
            }
        }
        catch (Exception e){
            return "Something went wrong.";
        }
    }


    @GetMapping("urldata")
    public HashMap<String,String> getUrlData(@RequestParam int userId){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()) {
            String query = "select long_url,short_url from url where userId=?";
            PreparedStatement preparedStatement =connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            ResultSet resultSet=preparedStatement.executeQuery();
            HashMap <String,String> hashMap=new HashMap<String,String>();
            while (resultSet.next()){
                hashMap.put(resultSet.getString(2),resultSet.getString(1));
            }
            return hashMap;
        }
        catch (Exception e){
            return null;
        }
    }
}

