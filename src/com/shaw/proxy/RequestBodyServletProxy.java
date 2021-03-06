package com.shaw.proxy;

import com.shaw.log.Logger;
import com.shaw.log.LoggerFactory;
import com.shaw.model.ControllerModel;
import com.shaw.note.AutoBody;
import com.shaw.pub.Constants;
import com.shaw.utils.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Created by Administrator on 2017/4/7 0007.
 */

/**
 * 参数注入 和 MVC核心代码
 * 本类 的class 和 method 都是在加载Listener时保存的
 * 本类只负责 Controller 里注解函数的调用 以及注解函数的参数注入
 * */
public class RequestBodyServletProxy extends HttpServlet {

    private Class clazz;
    private Method method;

    private static Logger logger = LoggerFactory.getLogger(RequestBodyServletProxy.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        postRequest(req, resp);

        ControllerModel controllerModel = Constants.controllerMapper.get(req.getServletPath());
        this.clazz = controllerModel.getClazz();
        this.method = controllerModel.getMethod();

        String targetPage = null;

        //获取函数返回数据类型
        //Type returnType = method.getGenericReturnType();

        //通过静态代理来初始化对象
        Object object = null;
        try {
            object= clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        Object[] objParameters = new Object[method.getParameters().length];


        //通过反射获取函数的参数 来给注解的参数注入
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            //反射参数对象
            Object tempObj = null;
            //判断参数是否是request
            if (parameters[i].getType() == HttpServletRequest.class) {
                objParameters[i] = req;
                continue;
            }
            //判断参数是否是response
            if (parameters[i].getType() == HttpServletResponse.class) {
                objParameters[i] = resp;
                continue;
            }

            //参数是否被AutoBody注解
            if (parameters[i].isAnnotationPresent(AutoBody.class)) {
                AutoBody autoBody = parameters[i].getAnnotation(AutoBody.class);
                try {
                    tempObj = StringUtils.requestConvertPOJO(req, parameters[i].getType(), autoBody.value());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                objParameters[i] = tempObj;
                continue;
            }
        }


        //通过静态代理来调用函数
        if (object != null) {
            try {
                targetPage = (String) method.invoke(object, objParameters);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        preRequest(req, resp);
        req.getRequestDispatcher(targetPage).forward(req, resp);
        super.doPost(req, resp);
    }

    /**
     *  真实角色操作前的附加操作
     */
    private void postRequest(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub

    }

    /**
     *  真实角色操作后的附加操作
     */
    private void preRequest(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub

    }

}
