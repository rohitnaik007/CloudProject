package com.example.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.example.repository.UserRepository;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.spi.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.example.model.User;
import com.example.service.UserService;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.util.Date;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private Environment environment;

    @Value("${spring.profiles.active:aws}")
    private String activeProfile;

    @Value("${appAccessKeyID}")
    private String accessKey;

    @Value("${appSecretAccessKey}")
    private String secretKey;

    @Value("${appImageBucketName}")
    private String bucketName;

    static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value={"/", "/login"}, method = GET)
    public ModelAndView login() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        //modelAndView.setViewName("login");
        logger.info("log data from log4j debug \n Login Method");
        return modelAndView;
    }


    @RequestMapping(value = "/registration", method = GET)
    public ModelAndView registration() {
        ModelAndView modelAndView = new ModelAndView();
        User user = new User();
        modelAndView.addObject("user", user);
        modelAndView.setViewName("registration");
        logger.info("log data from log4j debug \n Registration Method");
        return modelAndView;
    }


    @RequestMapping(value = "/viewall", method = GET)
    public ModelAndView viewAll() {
        ModelAndView modelAndView = new ModelAndView();
        //User user = new User();
        //Repository u1 = new UserRepository();
        //modelAndView.addObject("allusers", userService.getAllUsers());
        modelAndView.setViewName("viewall");
        return modelAndView;
    }


    @RequestMapping(value = "/forgot", method = GET)
    public ModelAndView forgotPage() {
        ModelAndView modelAndView = new ModelAndView();
        //User user = new User();
        //Repository u1 = new UserRepository();
        //modelAndView.addObject("allusers", userService.getAllUsers());
        modelAndView.setViewName("forgotpassword");
        return modelAndView;
    }


    @RequestMapping(value = "/forgotpass", method = RequestMethod.POST)
    public ModelAndView forgotPassword(WebRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String userName = request.getParameter("email");
        System.out.println("Send reset link to: "+userName);

        AmazonSNSClient snsClient = new AmazonSNSClient(new InstanceProfileCredentialsProvider());
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        String topicArn = snsClient.createTopic("EmailTopic").getTopicArn();

        PublishRequest publishRequest = new PublishRequest(topicArn, userName);
        PublishResult publishResult = snsClient.publish(publishRequest);
       // response.setStatus(HttpServletResponse.SC_OK);
        modelAndView.setViewName("forgotemailsent");
        return modelAndView;
    }



    @RequestMapping(value = "/viewsearch", method = RequestMethod.POST)
    public ModelAndView viewSearch(WebRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(request.getParameter("searchemail"));
        modelAndView.addObject("userName", "You are viewing " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
        modelAndView.addObject("adminMessage", user.getAboutme());
        if (activeProfile.equalsIgnoreCase("aws")) {
            modelAndView.addObject("imageSrc", getImage(user.getEmail() + ".jpg"));
        } else {
            modelAndView.addObject("imageSrc", "images/beer.jpg");

        }
        modelAndView.setViewName("viewsearch");
        return modelAndView;
    }


    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createNewUser(@Valid User user, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        User userExists = userService.findUserByEmail(user.getEmail());
        if (userExists != null) {
            bindingResult
                    .rejectValue("email", "error.user",
                            "There is already a user registered with the email provided");
        }
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("registration");
        } else {
            userService.saveUser(user);
            modelAndView.addObject("successMessage", "User has been registered successfully");
            modelAndView.addObject("user", new User());
            modelAndView.setViewName("registration");

        }
        return modelAndView;
    }

    @RequestMapping(value = "/home", method = GET)
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
        modelAndView.addObject("adminMessage", "Have a good day !!!");
        modelAndView.setViewName("/home");
        return modelAndView;
    }

    @RequestMapping(value = "/aboutme", method = GET)
    public ModelAndView aboutme() {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        modelAndView.addObject("userName", "Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
        //modelAndView.addObject("adminMessage","Content Available Only for Users with Admin Role");
        modelAndView.addObject("user", user);
        modelAndView.addObject("adminMessage", user.getAboutme());
        if (activeProfile.equalsIgnoreCase("aws")) {
            try {
                modelAndView.addObject("imageSrc", getImage(user.getEmail() + ".jpg"));
            } catch (Exception e) {
                modelAndView.addObject("imageSrc", getImage(user.getEmail() + ".jpg"));
            }
        } else {
            modelAndView.addObject("imageSrc", "images/" + user.getEmail() + ".jpg");

        }


        modelAndView.setViewName("/aboutme");
        return modelAndView;
    }


    @RequestMapping(value = "/aboutmeedit", method = RequestMethod.POST)
    public ModelAndView updateUserAboutMe(WebRequest request,@RequestParam("fileName")MultipartFile fileNew) throws IOException {
        ModelAndView modelAndView = new ModelAndView();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByEmail(auth.getName());
        String SubmitAction = request.getParameter("submitAction");
        if (SubmitAction.trim().equalsIgnoreCase("editAboutMe")) {
            user.setAboutme(request.getParameter("aboutmetext"));
            userService.saveAboutMe(user);
            modelAndView.addObject("imageSrc", getImage(user.getEmail() + ".jpg"));
        }
        if (!activeProfile.equalsIgnoreCase("aws")) {
//System.out.println(this.environment.getActiveProfiles().);
            //CommonsMultipartFile fileInMemory = request.getParameter("fileName");
            if (SubmitAction.trim().equalsIgnoreCase("uploadNewImage")) {
            File file = new File("//home//rohit//Desktop//Images//" + request.getParameter("fileName"));
            //File file2 = new File("//home//rohit//Desktop//Cloud//webapp//cloud//target//classes//static//images//"+user.getEmail()+".jpg");
            File file2 = new File("images//" + user.getEmail() + ".jpg");
            try {
                Image image1 = ImageIO.read(file);
                BufferedImage image = toBufferedImage(image1);

                ImageIO.write(image, "jpeg", file2);  // ignore returned boolean
                modelAndView.addObject("imageSrc", "images/" + user.getEmail() + ".jpg");
            } catch (IOException e) {
                System.out.println("Write error for " + file2.getPath() +
                        ": " + e.getMessage());
            }
        }
        } else {
            if (SubmitAction.trim().equalsIgnoreCase("uploadNewImage")) {
            if(fileNew != null) {
                if (!fileNew.getOriginalFilename().trim().equalsIgnoreCase("")) {
                    File myFile = new File(fileNew.getOriginalFilename());
                    String fileName = myFile.getName();
                    System.out.println("Absolute Path of the file ======= " + myFile.getAbsolutePath());
                    myFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(myFile);
                    fos.write(fileNew.getBytes());
                    fos.close();
                    uploadImage(user.getEmail() + ".jpg", myFile.getAbsolutePath());
                    modelAndView.addObject("imageSrc", getImage(user.getEmail() + ".jpg"));
                }
            }
            }
            if (SubmitAction.trim().equalsIgnoreCase("removeOldImage")) {
            removeImage(user.getEmail() + ".jpg");
                modelAndView.addObject("imageSrc", "images/beer.jpg");
            }

        }
        modelAndView.addObject("userName", "Welcome " + user.getName() + " " + user.getLastName() + " (" + user.getEmail() + ")");
        modelAndView.addObject("adminMessage", user.getAboutme());
        modelAndView.setViewName("/aboutme");
        return modelAndView;

    }


    public BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public void uploadImage(String filename, String filepath) {

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

//        AmazonS3 s3client = new AmazonS3Client(credentials);
        AmazonS3 s3client = new AmazonS3Client();
        System.setProperty("com.amazonaws.services.s3.disableGetObjectMD5Validation", "true");
        s3client.putObject(new PutObjectRequest(bucketName, filename,
                new File(filepath))
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public String getImage(String filename) {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

//            AmazonS3 s3client = new AmazonS3Client(credentials);
            AmazonS3 s3client = new AmazonS3Client();
            final S3Object object = s3client.getObject(bucketName, filename);
            return object.getObjectContent().getHttpRequest().getURI().toString();
        } catch (Exception exeption) {
            return "images/beer.jpg";
        }
    }


    public void removeImage(String filename) {
        try {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

//            AmazonS3 s3client = new AmazonS3Client(credentials);
            AmazonS3 s3client = new AmazonS3Client();
            s3client.deleteObject(bucketName, filename);

        } catch (Exception exeption) {

        }
    }



}

