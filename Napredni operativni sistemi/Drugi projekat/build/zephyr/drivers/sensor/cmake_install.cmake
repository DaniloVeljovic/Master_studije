# Install script for directory: /home/danilo/zephyrproject/zephyr/drivers/sensor

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/usr/local")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Is this installation the result of a crosscompile?
if(NOT DEFINED CMAKE_CROSSCOMPILING)
  set(CMAKE_CROSSCOMPILING "TRUE")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for each subdirectory.
  include("/home/danilo/zephyrproject/zephyr/build/zephyr/drivers/sensor/grove/cmake_install.cmake")
  include("/home/danilo/zephyrproject/zephyr/build/zephyr/drivers/sensor/hts221/cmake_install.cmake")
  include("/home/danilo/zephyrproject/zephyr/build/zephyr/drivers/sensor/lis2dh/cmake_install.cmake")
  include("/home/danilo/zephyrproject/zephyr/build/zephyr/drivers/sensor/lis2mdl/cmake_install.cmake")
  include("/home/danilo/zephyrproject/zephyr/build/zephyr/drivers/sensor/lps22hb/cmake_install.cmake")
  include("/home/danilo/zephyrproject/zephyr/build/zephyr/drivers/sensor/lsm6dsl/cmake_install.cmake")

endif()

