using System.Net;

namespace facesdk_cs_tests
{
    public static class PathsConfig
    { static PathsConfig()
        {
            ServicePointManager.ServerCertificateValidationCallback += 
                (sender, cert, chain, sslPolicyErrors) => true;
        }
        public static readonly FaceSdk FaceSdk = new FaceSdk(Environment.GetEnvironmentVariable("ServiceLink") ?? "http://localhost:41101/");
        private static readonly string ProjectDir = GetProjectDirectory();
        private static readonly string FilesPath = Path.Combine(ProjectDir, "misc", "files");
        public static readonly string Face1Path = Path.Combine(FilesPath, "face1.jpg");
        public static readonly string Face2Path = Path.Combine(FilesPath, "face2.jpg");
        public static readonly string Face3Path = Path.Combine(FilesPath, "face3.jpg");
        public static readonly string LivePhotoPath = Path.Combine(FilesPath, "me.png");
        public static readonly string PrintedDocumentPath = Path.Combine(FilesPath, "printedDoc.png");
        public static readonly string DocumentWithLivePath = Path.Combine(FilesPath, "me_and_id.png");
        public static readonly string SeveralFacesImagePath = Path.Combine(FilesPath, "severalFaces.jpg");


        private static string GetProjectDirectory()
        {
            var assemblyLocation = Assembly.GetExecutingAssembly().Location;
            var binDirectory = Path.GetDirectoryName(assemblyLocation);
            var projectDirectory =
                Path.GetFullPath(Path.Combine(binDirectory ?? throw new InvalidOperationException(),
                    "../../../"));

            return projectDirectory;
        }

        public static byte[] ReadImageBytes(string path)
        {
            if (!File.Exists(path))
            {
                throw new FileNotFoundException($"File not found at path: {path}");
            }
            return File.ReadAllBytes(path);
        }

    }
}